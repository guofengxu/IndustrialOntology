package org.industrial.ontology.kernel.lucene;

import org.apache.lucene.analysis.Analyzer;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.IndexingAnalyzerFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-08
 */
public class IndexingAnalyzerFactory {

    private final Supplier<IndexingAnalyzerWrapper> indexingAnalyzerWrapperProvider;

    public IndexingAnalyzerFactory(Supplier<IndexingAnalyzerWrapper> indexingAnalyzerWrapperProvider) {
        this.indexingAnalyzerWrapperProvider = checkNotNull(indexingAnalyzerWrapperProvider);
    }

    @Nonnull
    public Analyzer get() {
        return indexingAnalyzerWrapperProvider.get();
    }
}
