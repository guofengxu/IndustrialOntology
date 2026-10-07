package org.industrial.ontology.kernel.lucene;



import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.BUILT_IN_FALSE;

import static org.industrial.ontology.kernel.lucene.EntityDocumentFieldNames.BUILT_IN_TRUE;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityBuiltInStatusDocumentAugmenter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-06
 */
public class EntityBuiltInStatusDocumentAugmenter implements EntityDocumentAugmenter {

    public EntityBuiltInStatusDocumentAugmenter() {
    }

    @Override
    public void augmentDocument(@Nonnull OWLEntity entity, @Nonnull Document document) {
        // Built-in status.  Not analyzed
        var builtIn = entity.isBuiltIn() ? BUILT_IN_TRUE : BUILT_IN_FALSE;
        document.add(new StringField(EntityDocumentFieldNames.BUILT_IN, builtIn, Field.Store.NO));
    }
}
