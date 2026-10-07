package org.industrial.ontology.kernel.match;



import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.match.Matcher;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.AnnotationPropertyMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Jun 2018
 */
public class AnnotationPropertyMatcher implements Matcher<OWLAnnotationProperty> {

    @Nonnull
    private final Matcher<IRI> iriMatcher;

    public AnnotationPropertyMatcher(@Nonnull Matcher<IRI> iriMatcher) {
        this.iriMatcher = checkNotNull(iriMatcher);
    }

    @Override
    public boolean matches(@Nonnull OWLAnnotationProperty value) {
        return iriMatcher.matches(value.getIRI());
    }
}
