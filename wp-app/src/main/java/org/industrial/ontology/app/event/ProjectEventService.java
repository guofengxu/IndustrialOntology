package org.industrial.ontology.app.event;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.kernel.event.EventBatch;
import org.industrial.ontology.kernel.event.ProjectEventManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * A project's events (docs/01 §5.1 and §5.2, stage S8): read once after a tag, the legacy {@code GetProjectEvents}
 * that clients polled, or streamed as they are posted. Both need {@code ViewProject}; the legacy handler returned an
 * empty list instead of refusing.
 * <p>
 * A tag names a batch of events, such as those of one change; clients pass the last tag they received and get the
 * batches after it. Batches are kept for {@code webprotege.events.retention}. The request is refused with
 * {@value #EVENTS_EXPIRED} (410), and the client reloads what it shows, when some of the batches after the tag are no
 * longer kept, when the tag is of another epoch or ahead of the project's (the project has been loaded again since,
 * which restarts its tags), or when the client is so far behind that catching up would take more than
 * {@code webprotege.events.max-queued-batches} batches or {@value #MAX_BACKLOG_EVENTS} events.
 */
public class ProjectEventService {

    /**
     * Events after the tag are no longer available; the client must reload the project's data.
     */
    public static final String EVENTS_EXPIRED = "EVENTS_EXPIRED";

    /**
     * The most events that a client catches up with; one further behind reloads.
     */
    public static final int MAX_BACKLOG_EVENTS = 10_000;

    private final ProjectContexts projects;

    private final ProjectEventBroadcaster broadcaster;

    public ProjectEventService(@Nonnull ProjectContexts projects, @Nonnull ProjectEventBroadcaster broadcaster) {
        this.projects = checkNotNull(projects);
        this.broadcaster = checkNotNull(broadcaster);
    }

    /**
     * The project's events after the position, in the order they were posted, without duplicates.
     *
     * @param since the client's position, or {@code null} for none: only the project's epoch and current tag are
     *              returned, from which the client can then read or stream
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@value #EVENTS_EXPIRED} (410) if the events after the position are not
     *                                   available
     */
    @Nonnull
    public EventsSince getEventsSince(@Nonnull UserId caller,
                                      @Nonnull ProjectId projectId,
                                      @Nullable EventPosition since) {
        var events = projects.open(caller, projectId, VIEW_PROJECT).events();
        var current = events.getCurrentTag();
        if (since == null) {
            return new EventsSince(projectId, events.epoch(), current, current, List.of());
        }
        var batches = backlog(events, projectId, since, current, broadcaster.maxQueuedBatches());
        var distinct = new LinkedHashSet<ProjectEvent>();
        batches.forEach(batch -> distinct.addAll(batch.events()));
        var tag = batches.isEmpty() ? since.tag() : batches.get(batches.size() - 1).tag();
        return new EventsSince(projectId, events.epoch(), since.tag(), tag, new ArrayList<>(distinct));
    }

    /**
     * Sends the project's events after the position to the sink, then each batch as it is posted, until the
     * subscription is closed. The sink also receives a ping every heartbeat; the subscription is closed when the
     * caller may no longer view the project, the sink's credentials are no longer good, the project is closed or the
     * client falls too far behind.
     *
     * @param since the client's position, or {@code null} for only the batches posted from now on
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@value #EVENTS_EXPIRED} (410) if the events after the position are not
     *                                   available; {@value ProjectEventBroadcaster#TOO_MANY_SUBSCRIPTIONS} (429) if
     *                                   the caller has too many open subscriptions
     */
    @Nonnull
    public ProjectEventBroadcaster.Subscription subscribe(@Nonnull UserId caller,
                                                          @Nonnull ProjectId projectId,
                                                          @Nullable EventPosition since,
                                                          @Nonnull ProjectEventSink sink) {
        checkNotNull(sink);
        var context = projects.open(caller, projectId, VIEW_PROJECT);
        return broadcaster.subscribe(context, caller, since, sink);
    }

    /**
     * The retained batches after the position, oldest first.
     *
     * @param current the project's current tag, read before the batches: a tag ahead of it cannot have come from this
     *                epoch
     * @throws WpException {@value #EVENTS_EXPIRED} (410) if they are not all available or too many
     */
    static List<EventBatch> backlog(ProjectEventManager events,
                                    ProjectId projectId,
                                    EventPosition since,
                                    EventTag current,
                                    int maxBatches) {
        if (since.epoch() != null && !since.epoch().equals(events.epoch())) {
            throw expired(projectId, since, "the project has been loaded again since");
        }
        if (since.tag().compareTo(current) > 0) {
            throw expired(projectId, since, "the tag is ahead of the project's");
        }
        var batches = events.getEventBatchesAfter(since.tag());
        // Checked after reading the batches: a purge in between leaves the batches already read intact
        if (!events.isRetainedFrom(since.tag().next())) {
            throw expired(projectId, since, "some of them are no longer kept");
        }
        var eventCount = batches.stream().mapToLong(batch -> batch.events().size()).sum();
        if (batches.size() > maxBatches || eventCount > MAX_BACKLOG_EVENTS) {
            throw expired(projectId, since, "there are too many of them to catch up with");
        }
        return batches;
    }

    private static WpException expired(ProjectId projectId, EventPosition since, String reason) {
        return new WpException(EVENTS_EXPIRED, 410, "The events of project " + projectId.getId() + " after tag "
                + since.tag().getOrdinal() + " cannot be sent: " + reason + "; reload the project");
    }
}
