package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ConflictingBooleanValuesMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.ConflictingBooleanValuesMatcherFactory} (generated in the legacy build).
 */
public final class ConflictingBooleanValuesMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axioms;

    public ConflictingBooleanValuesMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axioms) {
        this.axioms = java.util.Objects.requireNonNull(axioms);
    }

    public ConflictingBooleanValuesMatcher create() {
        return new ConflictingBooleanValuesMatcher(axioms.get());
    }
}
