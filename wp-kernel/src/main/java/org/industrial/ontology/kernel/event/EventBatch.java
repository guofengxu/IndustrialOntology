package org.industrial.ontology.kernel.event;

import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.domain.event.ProjectEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The events that one post recorded under one tag, for example the high-level events of one change (docs/01 §5.2).
 * The SSE stream sends a batch as one message whose id is the tag, so that a client resuming from an id has received
 * either all of a batch's events or none of them.
 *
 * @param events without duplicates, in the order they were posted
 */
public record EventBatch(@Nonnull EventTag tag, @Nonnull List<ProjectEvent> events) {

    public EventBatch {
        checkNotNull(tag);
        events = List.copyOf(events);
    }
}
