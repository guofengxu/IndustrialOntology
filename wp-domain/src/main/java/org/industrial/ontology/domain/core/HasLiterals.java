package org.industrial.ontology.domain.core;



import org.semanticweb.owlapi.model.OWLLiteral;

import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasLiterals}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 02/12/2012
 */
public interface HasLiterals {
    /**
     * Gets the literals contained in the object which implements this interface.
     * @return A set of literals.
     */
    Set<OWLLiteral> getLiterals();
}
