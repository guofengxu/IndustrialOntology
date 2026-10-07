package org.industrial.ontology.domain.object;



import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Comparator;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.object.OWLIndividualSelector}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
public class OWLIndividualSelector extends OWLEntitySelector<OWLIndividual, OWLNamedIndividual> {

    public OWLIndividualSelector(Comparator<? super OWLNamedIndividual> entityComparator) {
        super(EntityType.NAMED_INDIVIDUAL, entityComparator);
    }

    @Override
    protected OWLIndividual toType(OWLNamedIndividual entity) {
        return entity;
    }
}
