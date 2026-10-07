package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link NonUniqueLangTagsMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.NonUniqueLangTagsMatcherFactory} (generated in the legacy build).
 */
public final class NonUniqueLangTagsMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axiomsIndex;

    public NonUniqueLangTagsMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axiomsIndex) {
        this.axiomsIndex = java.util.Objects.requireNonNull(axiomsIndex);
    }

    public NonUniqueLangTagsMatcher create(@Nonnull Matcher<OWLAnnotationProperty> propertyMatcher) {
        return new NonUniqueLangTagsMatcher(axiomsIndex.get(), propertyMatcher);
    }
}
