package org.industrial.ontology.kernel.render;



import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.renderer.ShortFormAdapter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Apr 2018
 */
public class ShortFormAdapter implements ShortFormProvider {

    @Nonnull
    private final DictionaryManager dictionaryManager;

    public ShortFormAdapter(@Nonnull DictionaryManager dictionaryManager) {
        this.dictionaryManager = checkNotNull(dictionaryManager);
    }

    @Nonnull
    @Override
    public String getShortForm(@Nonnull OWLEntity entity) {
        return dictionaryManager.getShortForm(entity);
    }

    @Override
    public void dispose() {

    }
}
