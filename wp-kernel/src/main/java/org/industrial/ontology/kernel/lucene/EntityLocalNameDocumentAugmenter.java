package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.domain.lang.LocalNameDictionaryLanguage;
import org.apache.lucene.document.Document;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.shortform.LocalNameExtractor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.EntityLocalNameDocumentAugmenter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-06
 */
public class EntityLocalNameDocumentAugmenter implements EntityDocumentAugmenter {

    @Nonnull
    private final LocalNameExtractor localNameExtractor;

    @Nonnull
    private final DictionaryLanguageFieldWriter fieldWriter;

    public EntityLocalNameDocumentAugmenter(@Nonnull LocalNameExtractor localNameExtractor,
                                            @Nonnull DictionaryLanguageFieldWriter fieldWriter) {
        this.localNameExtractor = checkNotNull(localNameExtractor);
        this.fieldWriter = checkNotNull(fieldWriter);
    }

    @Override
    public void augmentDocument(@Nonnull OWLEntity entity, @Nonnull Document document) {
        // Local name (IRI fragment of trailing path element)
        var entityIri = entity.getIRI();
        var localName = localNameExtractor.getLocalName(entityIri);
        if (!localName.isEmpty()) {
            fieldWriter.addFieldForDictionaryLanguage(document, LocalNameDictionaryLanguage.get(), localName);
        }
    }
}
