package org.industrial.ontology.domain.core;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasUnexpectedErrorMessage}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 * <p>
 *     An interface to an object that can provide a message explaining that an unexpected error occurred.
 * </p>
 */
public interface HasUnexpectedErrorMessage {

    /**
     * Gets the unexpected error message.
     * @return A {@link String} representing the unexpected error message.  Not {@code null}.
     */
    String getUnexpectedErrorMessage();
}
