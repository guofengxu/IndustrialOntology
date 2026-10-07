package org.industrial.ontology.domain.object;



import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.object.OWLObjectPropertyExpressionSelector}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
public class OWLObjectPropertyExpressionSelector extends OWLEntitySelector<OWLObjectPropertyExpression, OWLObjectProperty> {

    public OWLObjectPropertyExpressionSelector(Comparator<? super OWLObjectProperty> entityComparator) {
        super(EntityType.OBJECT_PROPERTY, entityComparator);
    }

    @Override
    protected OWLObjectPropertyExpression toType(OWLObjectProperty entity) {
        return entity;
    }
}
