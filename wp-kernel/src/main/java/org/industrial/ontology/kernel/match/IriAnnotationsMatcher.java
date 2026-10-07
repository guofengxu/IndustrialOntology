package org.industrial.ontology.kernel.match;



import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.IriAnnotationsMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
public class IriAnnotationsMatcher implements Matcher<IRI> {

    @Nonnull
    private final AnnotationAssertionAxiomsIndex axiomProvider;

    @Nonnull
    private final Matcher<OWLAnnotation> annotationMatcher;

    public IriAnnotationsMatcher(@Nonnull AnnotationAssertionAxiomsIndex axiomProvider,
                                 @Nonnull Matcher<OWLAnnotation> annotationMatcher) {
        this.axiomProvider = checkNotNull(axiomProvider);
        this.annotationMatcher = checkNotNull(annotationMatcher);
    }

    @Override
    public boolean matches(@Nonnull IRI subject) {
        return axiomProvider.getAnnotationAssertionAxioms(subject)
                            .map(OWLAnnotationAssertionAxiom::getAnnotation)
                            .anyMatch(annotationMatcher::matches);
    }
}
