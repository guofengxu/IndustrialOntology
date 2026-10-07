package org.industrial.ontology.kernel.lucene;



import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityDocumentAugmenter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-06
 */
public interface EntityDocumentAugmenter {

    void augmentDocument(@Nonnull OWLEntity entity, @Nonnull Document document);
}
