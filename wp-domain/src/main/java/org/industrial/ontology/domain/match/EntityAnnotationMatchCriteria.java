package org.industrial.ontology.domain.match;



import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityAnnotationMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10 Jun 2018
 */
public interface EntityAnnotationMatchCriteria extends EntityMatchCriteria {

    @Nonnull
    OWLAnnotationProperty getProperty();

    @Nonnull
    AnnotationPresence getPresence();
}
