package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EntityIsDeprecatedMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityIsDeprecatedMatcherFactory} (generated in the legacy build).
 */
public final class EntityIsDeprecatedMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axioms;

    public EntityIsDeprecatedMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axioms) {
        this.axioms = java.util.Objects.requireNonNull(axioms);
    }

    public EntityIsDeprecatedMatcher create() {
        return new EntityIsDeprecatedMatcher(axioms.get());
    }
}
