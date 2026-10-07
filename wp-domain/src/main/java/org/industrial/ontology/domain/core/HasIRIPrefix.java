package org.industrial.ontology.domain.core;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasIRIPrefix}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/08/2013
 * <p>
 *     An interface to an object that can provide an IRI prefix.
 * </p>
 */
public interface HasIRIPrefix {

    /**
     * Gets the IRI prefix.
     * @return A string representing the IRI prefix.  Not {@code null}.
     */
    String getIRIPrefix();
}
