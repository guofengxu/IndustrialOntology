package org.industrial.ontology.app.event;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.ProjectEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A project's events after a tag (docs/02 §5, {@code GET …/events?since=}), the legacy {@code ProjectEventList}.
 *
 * @param epoch  the epoch of the project's tags ({@link EventPosition})
 * @param since  the tag asked for
 * @param tag    the tag of the last batch returned, or {@code since} if there was none: the tag to ask from next
 * @param events the events of the batches after {@code since}, without duplicates
 */
public record EventsSince(@Nonnull ProjectId projectId,
                          @Nonnull String epoch,
                          @Nonnull EventTag since,
                          @Nonnull EventTag tag,
                          @Nonnull List<ProjectEvent> events) {

    public EventsSince {
        checkNotNull(projectId);
        checkNotNull(epoch);
        checkNotNull(since);
        checkNotNull(tag);
        events = List.copyOf(events);
    }
}
