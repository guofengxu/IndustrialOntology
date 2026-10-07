package org.industrial.ontology.domain.object;



import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.object.OWLDataPropertyExpressionSelector}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
public class OWLDataPropertyExpressionSelector extends OWLEntitySelector<OWLDataPropertyExpression, OWLDataProperty> {

    public OWLDataPropertyExpressionSelector( Comparator<? super OWLDataProperty> entityComparator) {
        super(EntityType.DATA_PROPERTY, entityComparator);
    }

    @Override
    protected OWLDataPropertyExpression toType(OWLDataProperty entity) {
        return entity;
    }
}
