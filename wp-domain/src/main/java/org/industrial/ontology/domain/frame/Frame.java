package org.industrial.ontology.domain.frame;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.Frame}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/01/2013
 * <p>
 *     A high level interface for frame objects.  All frames have some kind of subject.
 * </p>
 */
public interface Frame<S> {

    /**
     * Gets the subject of this frame.
     * @return The subject. Not {@code null}.
     */
    S getSubject();

    @Nonnull
    PlainEntityFrame toPlainFrame();
}
