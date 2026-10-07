package org.industrial.ontology.kernel.frame;



import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.ClassFrameProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public interface ClassFrameProvider {

    /**
     * Gets the class frame for the specified subject.
     * @param subject The subject.
     * @return The class frame for the subject
     */
    @Nonnull
    PlainClassFrame getFrame(@Nonnull OWLClass subject,
                             @Nonnull ClassFrameTranslationOptions options);
}
