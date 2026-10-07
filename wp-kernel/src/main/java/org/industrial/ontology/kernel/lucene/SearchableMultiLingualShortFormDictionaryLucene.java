package org.industrial.ontology.kernel.lucene;



import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.apache.lucene.queryparser.classic.ParseException;
import org.semanticweb.owlapi.model.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.shortform.EntityShortFormMatches;
import org.industrial.ontology.kernel.api.shortform.SearchString;
import org.industrial.ontology.kernel.api.shortform.SearchableMultiLingualShortFormDictionary;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.SearchableMultiLingualShortFormDictionaryLuceneImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public class SearchableMultiLingualShortFormDictionaryLucene implements SearchableMultiLingualShortFormDictionary {

    private static final Logger logger = LoggerFactory.getLogger(SearchableMultiLingualShortFormDictionaryLucene.class);

    @Nonnull
    private final LuceneIndex luceneIndex;

    public SearchableMultiLingualShortFormDictionaryLucene(@Nonnull LuceneIndex luceneIndex) {
        this.luceneIndex = checkNotNull(luceneIndex);
    }

    @Nonnull
    @Override
    public Page<EntityShortFormMatches> getShortFormsContaining(@Nonnull List<SearchString> searchStrings,
                                                                @Nonnull Set<EntityType<?>> entityTypes,
                                                                @Nonnull List<DictionaryLanguage> languages,
                                                                @Nonnull ImmutableList<EntitySearchFilter> searchFilters,
                                                                @Nonnull PageRequest pageRequest) {
        try {
            var stopwatch = Stopwatch.createStarted();
            // TODO: Rewrite entity types
            var entities = luceneIndex.search(searchStrings, languages, searchFilters, entityTypes, pageRequest);
            var elapsedTimeMs = stopwatch.elapsed().toMillis();
            if(entities.isPresent()) {
                var resultsPage = entities.get();
                logger.info("Found {} results in {} ms", resultsPage.getTotalElements(), elapsedTimeMs);
            }
            return entities.orElse(Page.emptyPage());
        } catch (IOException | ParseException e) {
            logger.error("Error performing search", e);
            return Page.emptyPage();
        }
    }
}
