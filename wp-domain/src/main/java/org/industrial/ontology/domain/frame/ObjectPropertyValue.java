package org.industrial.ontology.domain.frame;



import org.industrial.ontology.domain.entity.OWLObjectPropertyData;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ObjectPropertyValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
public abstract class ObjectPropertyValue extends PropertyValue {

    @Override
    public abstract OWLObjectPropertyData getProperty();
}
