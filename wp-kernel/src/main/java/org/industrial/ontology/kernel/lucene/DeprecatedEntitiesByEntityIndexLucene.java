package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.kernel.api.index.DeprecatedEntitiesByEntityIndex;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;

import java.io.IOException;
import java.io.UncheckedIOException;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.DeprecatedEntitiesByEntityIndexLuceneImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-04
 */
public class DeprecatedEntitiesByEntityIndexLucene implements DeprecatedEntitiesByEntityIndex {

    @Nonnull
    private final SearcherManager searcherManager;

    @Nonnull
    private final LuceneEntityDocumentTranslator luceneEntityDocumentTranslator;

    public DeprecatedEntitiesByEntityIndexLucene(@Nonnull SearcherManager searcherManager,
                                                     @Nonnull LuceneEntityDocumentTranslator luceneEntityDocumentTranslator) {
        this.searcherManager = checkNotNull(searcherManager);
        this.luceneEntityDocumentTranslator = checkNotNull(luceneEntityDocumentTranslator);
    }

    @Override
    public boolean isDeprecated(@Nonnull OWLEntity entity) {
        try {
            return isDeprecatedEntity(entity);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isDeprecatedEntity(@Nonnull OWLEntity entity) throws IOException {
        var indexSearcher = searcherManager.acquire();
        try {
            var entityQuery = luceneEntityDocumentTranslator.getEntityDocumentQuery(entity);
            var deprecatedQuery = new TermQuery(new Term(EntityDocumentFieldNames.DEPRECATED, EntityDocumentFieldNames.DEPRECATED_TRUE));
            var entityDeprecatedQuery = new BooleanQuery.Builder()
                    .add(entityQuery, BooleanClause.Occur.MUST)
                    .add(deprecatedQuery, BooleanClause.Occur.MUST)
                    .build();
            var totalHits = indexSearcher.search(entityDeprecatedQuery, 1).totalHits;
            return totalHits.value > 0;
        } finally {
            searcherManager.release(indexSearcher);
        }
    }
}
