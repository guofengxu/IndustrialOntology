package org.industrial.ontology.kernel.index;

import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.Key}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-07
 */
public record Key<T>(@Nonnull OWLOntologyID ontologyId, @Nonnull T value) {

    public Key {
        Objects.requireNonNull(ontologyId, "Null ontologyId");
        Objects.requireNonNull(value, "Null value");
    }

    public static <T> Key<T> get(@Nonnull OWLOntologyID ontologyId, @Nonnull T value) {
        return new Key<>(ontologyId, value);
    }

    @Nonnull
    public OWLOntologyID getOntologyId() {
        return ontologyId;
    }

    @Nonnull
    public T getValue() {
        return value;
    }
}
