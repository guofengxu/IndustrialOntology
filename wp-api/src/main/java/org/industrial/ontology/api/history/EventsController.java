package org.industrial.ontology.api.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.apikey.ApiKeyParser;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.event.EventPosition;
import org.industrial.ontology.app.event.EventsSince;
import org.industrial.ontology.app.event.ProjectEventBroadcaster;
import org.industrial.ontology.app.event.ProjectEventService;
import org.industrial.ontology.app.event.ProjectEventSink;
import org.industrial.ontology.app.project.ProjectRuntimeProperties;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.kernel.event.EventBatch;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A project's events (docs/02 §5, stage S8): read once after a tag, or streamed as server-sent events. Both need
 * {@code ViewProject}, which the service checks.
 * <p>
 * A position in the events is written {@code <epoch>-<tag>}: the tag of the last batch received and the epoch of the
 * project's tags, which restart when the project is loaded again. The stream sends each batch of events (for example
 * those of one change) as one message: {@code id} is its position, {@code event} is {@code events} and {@code data} is
 * {@code {"epoch": e, "tag": n, "events": [...]}}, each event with its type name in {@code type}. A comment
 * ({@code :ping}) is sent every heartbeat. A client that reconnects with {@code Last-Event-ID} (or {@code since})
 * receives the batches after that position, none twice; a bare tag, such as the event tag of a change, is accepted
 * too, without the check of its epoch. A position whose later events are no longer kept, too many to catch up with,
 * or of another epoch is refused with 410 {@code EVENTS_EXPIRED}: the client reloads the project's data and starts
 * from the current position.
 * <p>
 * A stream ends when its token expires and is checked with the user's permission while open: one opened with an API
 * key ends once the key is deleted.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/events")
@Tag(name = "Events", description = "A project's events, read after a tag or streamed")
public class EventsController {

    /**
     * The SSE event name of a batch.
     */
    static final String BATCH_EVENT = "events";

    private static final Pattern POSITION = Pattern.compile("(?:([0-9a-f]{1,32})-)?(\\d{1,10})");

    private final ProjectEventService eventService;

    private final ApiKeyService apiKeyService;

    private final ProjectRuntimeProperties properties;

    public EventsController(ProjectEventService eventService,
                            ApiKeyService apiKeyService,
                            ProjectRuntimeProperties properties) {
        this.eventService = checkNotNull(eventService);
        this.apiKeyService = checkNotNull(apiKeyService);
        this.properties = checkNotNull(properties);
    }

    @Operation(summary = "The events after a position, and the position to read from next (permission ViewProject)",
               description = "Without since, only the current position. 410 EVENTS_EXPIRED if the events after since "
                       + "are not available.")
    @GetMapping
    public EventsSinceDto since(@Caller UserId caller,
                                @PathVariable String projectId,
                                @RequestParam(required = false) String since) {
        return EventsSinceDto.of(eventService.getEventsSince(caller, ProjectIds.parse(projectId), position(since)));
    }

    @Operation(summary = "Streams the events after a position as server-sent events (permission ViewProject)",
               description = "Last-Event-ID takes precedence over since; with neither, the stream starts at the "
                       + "current position. 410 EVENTS_EXPIRED if the events after the position are not available, "
                       + "429 TOO_MANY_SUBSCRIPTIONS if the caller has too many open streams.")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@Caller UserId caller,
                             @PathVariable String projectId,
                             @RequestParam(required = false) String since,
                             @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId,
                             HttpServletRequest request,
                             HttpServletResponse response) {
        var from = position(lastEventId != null && !lastEventId.isBlank() ? lastEventId : since);
        var timeout = properties.events().streamTimeout();
        BooleanSupplier credentialsValid = () -> true;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwt && jwt.getToken().getExpiresAt() != null) {
            var expiresAt = jwt.getToken().getExpiresAt();
            credentialsValid = () -> Instant.now().isBefore(expiresAt);
            var untilExpiry = Duration.between(Instant.now(), expiresAt);
            if (untilExpiry.compareTo(timeout) < 0) {
                timeout = untilExpiry.isNegative() ? Duration.ofSeconds(1) : untilExpiry;
            }
        } else {
            var apiKey = ApiKeyParser.parseApiKey(request.getHeader(HttpHeaders.AUTHORIZATION));
            if (apiKey.isPresent()) {
                credentialsValid = () -> apiKeyService.getUserIdForApiKey(apiKey.get())
                                                      .filter(caller::equals)
                                                      .isPresent();
            }
        }
        var emitter = new SseEmitter(timeout.toMillis());
        var subscription = new AtomicReference<ProjectEventBroadcaster.Subscription>();
        Runnable unsubscribe = () -> {
            var open = subscription.get();
            if (open != null) {
                open.close();
            }
        };
        emitter.onCompletion(unsubscribe);
        emitter.onTimeout(unsubscribe);
        emitter.onError(error -> unsubscribe.run());
        subscription.set(eventService.subscribe(caller, ProjectIds.parse(projectId), from,
                                                new EmitterSink(emitter, credentialsValid)));
        // Proxies such as nginx would otherwise hold the events back
        response.setHeader("X-Accel-Buffering", "no");
        return emitter;
    }

    @Nullable
    private static EventPosition position(@Nullable String position) {
        if (position == null || position.isBlank()) {
            return null;
        }
        var matcher = POSITION.matcher(position.strip());
        if (matcher.matches()) {
            try {
                return new EventPosition(matcher.group(1), EventTag.get(Integer.parseInt(matcher.group(2))));
            } catch (NumberFormatException e) {
                // refused below
            }
        }
        throw WpException.invalidRequest("An event position is a tag from 0, optionally after its epoch and a hyphen: "
                                                 + position);
    }

    private static String id(String epoch, EventTag tag) {
        return epoch + "-" + tag.getOrdinal();
    }

    /**
     * Writes a subscription's batches to the emitter; sends before the emitter is returned are buffered by Spring.
     */
    private record EmitterSink(SseEmitter emitter, BooleanSupplier credentials) implements ProjectEventSink {

        @Override
        public void send(@Nonnull String epoch, @Nonnull EventBatch batch) throws IOException {
            emitter.send(SseEmitter.event()
                                   .id(id(epoch, batch.tag()))
                                   .name(BATCH_EVENT)
                                   .data(new EventBatchDto(epoch, batch.tag().getOrdinal(), batch.events()),
                                         MediaType.APPLICATION_JSON));
        }

        @Override
        public void ping() throws IOException {
            emitter.send(SseEmitter.event().comment("ping"));
        }

        @Override
        public boolean credentialsValid() {
            return credentials.getAsBoolean();
        }

        @Override
        public void close() {
            emitter.complete();
        }
    }

    /**
     * @param position the position to read or stream from next, {@code <epoch>-<tag>}
     * @param tag      the tag of {@code position}
     * @param events   the events after {@code since}, each with its type name in {@code type}
     */
    public record EventsSinceDto(@Nonnull String projectId,
                                 @Nonnull String epoch,
                                 int since,
                                 int tag,
                                 @Nonnull String position,
                                 @Nonnull List<ProjectEvent> events) {

        static EventsSinceDto of(EventsSince events) {
            return new EventsSinceDto(events.projectId().getId(), events.epoch(), events.since().getOrdinal(),
                                      events.tag().getOrdinal(), id(events.epoch(), events.tag()), events.events());
        }
    }

    public record EventBatchDto(@Nonnull String epoch, int tag, @Nonnull List<ProjectEvent> events) {
    }
}
