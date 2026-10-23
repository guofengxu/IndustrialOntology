package org.industrial.ontology.app.event;

import org.industrial.ontology.domain.event.EventTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Where a client is in a project's events: the last tag it received and, if it knows it, the epoch of that tag. A
 * project's tags restart each time it is loaded, so a tag from another epoch is refused instead of being taken for a
 * tag of this one (stage S8).
 *
 * @param epoch the epoch of the tag, or {@code null} when the client has only a tag (such as the event tag that a
 *              change returns); then only a tag ahead of the project's is found out
 */
public record EventPosition(@Nullable String epoch, @Nonnull EventTag tag) {

    public EventPosition {
        checkNotNull(tag);
    }

    /**
     * A position known by its tag only.
     */
    @Nonnull
    public static EventPosition of(@Nonnull EventTag tag) {
        return new EventPosition(null, tag);
    }
}
