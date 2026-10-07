package org.industrial.ontology.kernel.lucene;



import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.LuceneIndexUpdater}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-07
 */
public interface LuceneIndexUpdater {

    void updateIndexForEntities(@Nonnull Collection<OWLEntity> entities);
}
