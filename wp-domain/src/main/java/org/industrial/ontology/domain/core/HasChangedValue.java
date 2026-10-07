package org.industrial.ontology.domain.core;



import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasChangedValue}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 18/12/2012
 */
public interface HasChangedValue<T> {

    Optional<T> getOldValue();

    Optional<T> getNewValue();
}
