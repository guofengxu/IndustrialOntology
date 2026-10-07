package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.semanticweb.owlapi.model.OWLAnnotation;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link IriAnnotationsMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.IriAnnotationsMatcherFactory} (generated in the legacy build).
 */
public final class IriAnnotationsMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axiomProvider;

    public IriAnnotationsMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axiomProvider) {
        this.axiomProvider = java.util.Objects.requireNonNull(axiomProvider);
    }

    public IriAnnotationsMatcher create(@Nonnull Matcher<OWLAnnotation> annotationMatcher) {
        return new IriAnnotationsMatcher(axiomProvider.get(), annotationMatcher);
    }
}
