package org.industrial.ontology.domain.frame;



import org.industrial.ontology.domain.entity.OWLDataPropertyData;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.DataPropertyValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
public abstract class DataPropertyValue extends PropertyValue {

    @Override
    public abstract OWLDataPropertyData getProperty();
}
