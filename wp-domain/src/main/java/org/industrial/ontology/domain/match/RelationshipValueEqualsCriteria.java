package org.industrial.ontology.domain.match;



import org.semanticweb.owlapi.model.OWLPrimitive;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipValueEqualsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-04
 */
public interface RelationshipValueEqualsCriteria extends RelationshipValueCriteria {

    @Nonnull
    OWLPrimitive getValue();
}
