package org.industrial.ontology.kernel.lucene;



import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityBasedSimilarity}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-11
 */
public class EntityBasedSimilarity extends SimilarityBase {

    public EntityBasedSimilarity() {
    }

    @Override
    protected double score(BasicStats stats, double freq, double docLen) {
        return freq * stats.getBoost();
    }

    @Override
    public String toString() {
        return EntityBasedSimilarity.class.getSimpleName();
    }
}
