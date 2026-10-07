package org.industrial.ontology.domain.core;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasDisplayName}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 * <p>
 *     An interface to an object that has a display name.
 * </p>
 */
public interface HasDisplayName {

    /**
     * Gets the display name for this object.
     * @return A string representing the display name for this object.  Not {@code null}.
     */
    String getDisplayName();
}
