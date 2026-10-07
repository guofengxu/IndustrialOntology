package org.industrial.ontology.domain.core;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.HasResult}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/02/2013
 * <p>
 *     An interface to objects that can produce a result.
 * </p>
 */
public interface HasResult<T> {

    /**
     * Gets the result.
     * @return The result.  Not {@code null}.
     */
    @Nonnull
    T getResult();

}
