package org.industrial.ontology.domain.entity;



import org.semanticweb.owlapi.model.OWLProperty;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.OWLPropertyData}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/12/2012
 */
public abstract class OWLPropertyData extends OWLEntityData {

    public abstract boolean isOWLAnnotationProperty();

    @Override
    public OWLProperty getEntity() {
        return (OWLProperty) super.getEntity();
    }
}
