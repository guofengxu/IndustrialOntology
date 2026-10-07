package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link AnnotationValuesAreNotDisjointMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.AnnotationValuesAreNotDisjointMatcherFactory} (generated in the legacy build).
 */
public final class AnnotationValuesAreNotDisjointMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axioms;

    public AnnotationValuesAreNotDisjointMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axioms) {
        this.axioms = java.util.Objects.requireNonNull(axioms);
    }

    public AnnotationValuesAreNotDisjointMatcher create(@Nonnull Matcher<OWLAnnotationProperty> propertyA, @Nonnull Matcher<OWLAnnotationProperty> propertyB) {
        return new AnnotationValuesAreNotDisjointMatcher(axioms.get(), propertyA, propertyB);
    }
}
