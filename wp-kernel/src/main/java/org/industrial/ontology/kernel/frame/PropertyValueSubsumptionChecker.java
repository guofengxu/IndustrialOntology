package org.industrial.ontology.kernel.frame;



import org.industrial.ontology.domain.frame.PlainPropertyValue;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.PropertyValueSubsumptionChecker}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 26/02/2014
 */
public interface PropertyValueSubsumptionChecker {

    boolean isSubsumedBy(PlainPropertyValue propertyValueA, PlainPropertyValue propertyValueB);
}
