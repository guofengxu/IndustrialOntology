package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import org.industrial.ontology.kernel.api.shortform.MultilingualDictionaryUpdater;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LuceneMultiLingualDictionaryUpdater}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-13
 */
public class LuceneMultiLingualDictionaryUpdater implements MultilingualDictionaryUpdater {

    @Nonnull
    private final LuceneIndexUpdater indexUpdater;

    public LuceneMultiLingualDictionaryUpdater(@Nonnull LuceneIndexUpdater indexUpdater) {
        this.indexUpdater = indexUpdater;
    }

    @Override
    public void update(@Nonnull Collection<OWLEntity> entities,
                       @Nonnull List<DictionaryLanguage> languages) {
        indexUpdater.updateIndexForEntities(entities);
    }
}
