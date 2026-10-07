package org.industrial.ontology.domain.core;



import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasEntity}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/03/2013
 */
public interface HasEntity {

    /**
     * Gets the entity associated with this object.
     * @return The entity. Not {@code null}.
     */
    OWLEntity getEntity();
}
