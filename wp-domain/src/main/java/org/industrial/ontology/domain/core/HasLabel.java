package org.industrial.ontology.domain.core;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasLabel}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 25/01/2012
 * <p>
 *     An interface for things that have labels
 * </p>
 */
public interface HasLabel {

    /**
     * Gets the label of this thing.
     * @return A string representing the label
     */
    String getLabel();
}
