package org.industrial.ontology.app.project;

import org.industrial.ontology.domain.event.EventTag;
import org.industrial.ontology.kernel.api.revision.Revision;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * What a change to a project's ontologies returns (docs/02 §1): the use case's result, the revision that recorded the
 * changes, and the project's event tag after them, with which a client can tell whether it has already received the
 * events of the change.
 *
 * @param revision empty when no change was effective, so that nothing was recorded
 */
public record ChangeOutcome<T>(@Nonnull T result, @Nonnull Optional<Revision> revision, @Nonnull EventTag eventTag) {

    public ChangeOutcome {
        checkNotNull(result);
        checkNotNull(revision);
        checkNotNull(eventTag);
    }

    /**
     * The same outcome with another result, for example a rendering of the entities that were created.
     */
    @Nonnull
    public <U> ChangeOutcome<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
        return new ChangeOutcome<>(mapper.apply(result), revision, eventTag);
    }
}
