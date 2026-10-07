package org.industrial.ontology.kernel.lucene;



import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.shortform.MultiLingualShortFormDictionary;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.MultiLingualShortFormDictionaryLuceneImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public class MultiLingualShortFormDictionaryLucene implements MultiLingualShortFormDictionary {

    private final Logger logger = LoggerFactory.getLogger(MultiLingualShortFormDictionaryLucene.class);

    @Nonnull
    private final LuceneIndex luceneIndex;

    public MultiLingualShortFormDictionaryLucene(@Nonnull LuceneIndex luceneIndex) {
        this.luceneIndex = checkNotNull(luceneIndex);
    }

    @Nonnull
    @Override
    public String getShortForm(@Nonnull OWLEntity entity,
                               @Nonnull List<DictionaryLanguage> languages,
                               @Nonnull String defaultShortForm) {
        try {
            return luceneIndex.find(entity, languages)
                              .findFirst()
                              .flatMap(entityShortForms -> getShortFormForLanguages(languages, entityShortForms))
                              .orElse(defaultShortForm);
        } catch (IOException e) {
            logger.error("Error while looking up entity short forms", e);
            return defaultShortForm;
        }
    }

    public static Optional<String> getShortFormForLanguages(@Nonnull List<DictionaryLanguage> languages,
                                                            @Nonnull EntityShortForms entityShortForms) {
        var shortFormsByDictionaryLanguage = entityShortForms.getShortForms();
        return languages.stream().map(shortFormsByDictionaryLanguage::get).filter(Objects::nonNull).findFirst();
    }

    @Nonnull
    @Override
    public ImmutableMap<DictionaryLanguage, String> getShortForms(@Nonnull OWLEntity entity,
                                                                  @Nonnull List<DictionaryLanguage> languages) {
        try {
            return luceneIndex.find(entity, languages)
                              .findFirst()
                              .map(EntityShortForms::getShortForms)
                              .orElse(ImmutableMap.of());
        } catch (IOException e) {
            logger.error("Error while looking up entity short forms");
            return ImmutableMap.of();
        }
    }
}
