package org.industrial.ontology.domain.object;



import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Comparator;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.object.OWLClassExpressionSelector}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
public class OWLClassExpressionSelector extends OWLEntitySelector<OWLClassExpression, OWLClass> {

    public OWLClassExpressionSelector(Comparator<? super OWLClass> entityComparator) {
        super(EntityType.CLASS, entityComparator);
    }

    @Override
    protected OWLClassExpression toType(OWLClass entity) {
        return entity;
    }
}
