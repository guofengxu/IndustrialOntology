package org.industrial.ontology.domain.core;



import org.industrial.ontology.domain.entity.OWLEntityData;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasEntityDataProvider}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 02/12/2012
 */
public interface HasEntityDataProvider {

    /**
     * Gets the {@link OWLEntityData} for a given {@link OWLEntity}.
     * @param entity The entity.  Not {@code null}.
     * @return An {@link Optional} of the {@link OWLEntityData} for the specified {@link OWLEntity}.
     */
    Optional<OWLEntityData> getEntityData(OWLEntity entity);
}
