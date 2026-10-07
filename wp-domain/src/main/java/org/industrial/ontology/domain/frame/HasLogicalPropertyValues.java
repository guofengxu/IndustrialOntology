package org.industrial.ontology.domain.frame;



import com.google.common.collect.ImmutableList;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.HasLogicalPropertyValues}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 09/12/2012
 */
public interface HasLogicalPropertyValues {

    ImmutableList<PropertyValue> getLogicalPropertyValues();
}
