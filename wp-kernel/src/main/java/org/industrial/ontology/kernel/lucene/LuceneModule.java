package org.industrial.ontology.kernel.lucene;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.project.ProjectDisposablesManager;
import org.industrial.ontology.kernel.api.util.DisposableObjectManager;
import org.industrial.ontology.domain.core.ProjectId;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.industrial.ontology.kernel.api.shortform.MultiLingualDictionary;

import org.industrial.ontology.kernel.api.shortform.MultiLingualShortFormDictionary;
import org.industrial.ontology.kernel.api.shortform.MultiLingualShortFormIndex;
import org.industrial.ontology.kernel.api.shortform.MultilingualDictionaryUpdater;
import org.industrial.ontology.kernel.api.shortform.SearchableMultiLingualShortFormDictionary;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LuceneModule}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-08
 */
public class LuceneModule {

    private static final Logger logger = LoggerFactory.getLogger(LuceneModule.class);

    public static final int MIN_GRAM_SIZE = 2;

    public static final int MAX_GRAM_SIZE = 11;

    public FieldNameTranslator provideDictionaryLanguage2FieldNameTranslator(FieldNameTranslatorImpl impl) {
        return impl;
    }

    LuceneEntityDocumentTranslator provideLuceneEntityDocumentTranslator(LuceneEntityDocumentTranslatorImpl impl) {
        return impl;
    }

    LuceneIndex provideLuceneIndex(LuceneIndexImpl impl,
                                   // Not this is needed here to force an initial write of the index
                                   LuceneIndexWriter indexWriter) {
        return impl;
    }

    LuceneIndexWriter provideLuceneIndexWriter(LuceneIndexWriterImpl impl) {
        try {
            impl.writeIndex();
            return impl;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    MultiLingualShortFormDictionary provideMultiLingualShortFormDictionary(MultiLingualShortFormDictionaryLucene impl) {
        return impl;
    }

    SearchableMultiLingualShortFormDictionary provideSearchableMultiLingualShortFormDictionary(SearchableMultiLingualShortFormDictionaryLucene impl) {
        return impl;
    }

    MultiLingualDictionary provideMultiLingualDictionary(MultiLingualDictionaryLucene impl) {
        return impl;
    }

    Directory provideDirectory(ProjectLuceneDirectoryPathSupplier pathSupplier) {
        try {
            // FSDirectory.open chooses the best implementation for the platform
            return FSDirectory.open(pathSupplier.get());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    IndexWriterConfig provideIndexWriterConfig(IndexingAnalyzerFactory analyzerFactory) {
        var analyzer = analyzerFactory.get();
        var config = new IndexWriterConfig(analyzer);
        return config.setSimilarity(new EntityBasedSimilarity());
    }

    @MinGramSize
    int provideMinGramSize() {
        return MIN_GRAM_SIZE;
    }

    @MaxGramSize
    int provideMaxGramSize() {
        return MAX_GRAM_SIZE;
    }

    IndexWriter provideIndexWriter(Directory directory,
                                   IndexWriterConfig indexWriterConfig,
                                   ProjectDisposablesManager projectDisposablesManager,
                                   ProjectId projectId) {
        try {
            var indexWriter = new IndexWriter(directory, indexWriterConfig);
            projectDisposablesManager.register(() -> {
                try {
                    indexWriter.close();
                    logger.info("{} Closed lucene index writer", projectId);
                } catch (IOException e) {
                    logger.error("Error when disposing of Project Lucene IndexWriter", e);
                }
            });

            return indexWriter;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    SearcherManager provideSearcherManager(IndexWriter indexWriter,
                                           SearcherFactory searcherFactory,
                                           DisposableObjectManager disposableObjectManager) {
        try {
            var searchManager = new SearcherManager(indexWriter, searcherFactory);
            disposableObjectManager.register(() -> {
                try {
                    searchManager.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return searchManager;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    SearcherFactory provideSearcherFactory() {
        return new SearcherFactory();
    }

    LuceneIndexUpdater provideLuceneIndexUpdater(LuceneIndexUpdaterImpl impl) {
        return impl;
    }

    MultilingualDictionaryUpdater provideMultilingualDictionaryUpdater(LuceneMultiLingualDictionaryUpdater impl) {
        return impl;
    }

    MultiLingualShortFormIndex provideMultiLingualShortFormIndex(MultiLingualShortFormIndexLucene impl) {
        return impl;
    }

    ImmutableList<EntitySearchFilterMatcher> provideSearchFilterMatchers(EntitySearchFilterMatchersFactory factory) {
        return factory.getSearchFilterMatchers();
    }
}
