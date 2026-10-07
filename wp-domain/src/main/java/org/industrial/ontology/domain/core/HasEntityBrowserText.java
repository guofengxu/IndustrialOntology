package org.industrial.ontology.domain.core;



import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasEntityBrowserText}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/02/2013
 */
public interface HasEntityBrowserText {

    /**
     * Gets the browser text for a specific entity.
     * @param entity The entity.  Not {@code null}.
     * @return An {@link Optional} which provides a {@link String} representig the browser text.
     * @throws NullPointerException if {@code entity} is {@code null}.
     */
    Optional<String> getBrowserText(OWLEntity entity);
}
