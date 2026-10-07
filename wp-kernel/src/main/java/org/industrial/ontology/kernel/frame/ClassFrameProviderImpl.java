package org.industrial.ontology.kernel.frame;



import org.industrial.ontology.kernel.frame.translator.Class2ClassFrameTranslatorFactory;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.ClassFrameProviderImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public class ClassFrameProviderImpl implements ClassFrameProvider {

    @Nonnull
    private final Class2ClassFrameTranslatorFactory translatorFactory;

    public ClassFrameProviderImpl(@Nonnull Class2ClassFrameTranslatorFactory classFrameTranslatorFactory) {
        this.translatorFactory = classFrameTranslatorFactory;
    }

    @Nonnull
    @Override
    public PlainClassFrame getFrame(@Nonnull OWLClass subject,
                                    @Nonnull ClassFrameTranslationOptions options) {
        var translator = translatorFactory.create(options);
        return translator.getFrame(subject);
    }
}
