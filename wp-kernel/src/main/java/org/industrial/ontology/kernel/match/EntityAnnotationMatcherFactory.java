package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.industrial.ontology.domain.match.AnnotationPresence;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.match.Matcher;
import org.semanticweb.owlapi.model.OWLAnnotation;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EntityAnnotationMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityAnnotationMatcherFactory} (generated in the legacy build).
 */
public final class EntityAnnotationMatcherFactory {

    private final Supplier<AnnotationAssertionAxiomsIndex> axiomProvider;

    public EntityAnnotationMatcherFactory(Supplier<AnnotationAssertionAxiomsIndex> axiomProvider) {
        this.axiomProvider = java.util.Objects.requireNonNull(axiomProvider);
    }

    public EntityAnnotationMatcher create(@Nonnull Matcher<OWLAnnotation> annotationMatcher, @Nonnull AnnotationPresence annotationPresence) {
        return new EntityAnnotationMatcher(axiomProvider.get(), annotationMatcher, annotationPresence);
    }
}
