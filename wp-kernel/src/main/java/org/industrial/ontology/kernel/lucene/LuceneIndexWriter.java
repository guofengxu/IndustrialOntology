package org.industrial.ontology.kernel.lucene;



import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LuceneIndexWriter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public interface LuceneIndexWriter {

    void rebuildIndex() throws IOException;

    void writeIndex() throws IOException;
}
