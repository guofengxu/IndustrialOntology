package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;

import org.industrial.ontology.kernel.api.index.OntologyAnnotationsSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsSignatureIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.OntologySignatureByTypeIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-16
 */
public class OntologySignatureByTypeIndex implements org.industrial.ontology.kernel.api.index.OntologySignatureByTypeIndex, DependentIndex {

    @Nonnull
    private final OntologyAxiomsSignatureIndex ontologyAxiomsSignatureIndex;

    @Nonnull
    private final OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex;

    public OntologySignatureByTypeIndex(@Nonnull OntologyAxiomsSignatureIndex ontologyAxiomsSignatureIndex,
                                            @Nonnull OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex) {
        this.ontologyAxiomsSignatureIndex = checkNotNull(ontologyAxiomsSignatureIndex);
        this.ontologyAnnotationsSignatureIndex = checkNotNull(ontologyAnnotationsSignatureIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(ontologyAnnotationsSignatureIndex, ontologyAxiomsSignatureIndex);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <E extends OWLEntity> Stream<E> getSignature(@Nonnull EntityType<E> type,
                                                        @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(type);
        checkNotNull(ontologyId);
        if(type.equals(EntityType.ANNOTATION_PROPERTY)) {
            return Stream.<E>concat(ontologyAxiomsSignatureIndex.getOntologyAxiomsSignature(type, ontologyId),
                                    (Stream<E>) ontologyAnnotationsSignatureIndex.getOntologyAnnotationsSignature(ontologyId));
        }
        else {
            return ontologyAxiomsSignatureIndex.getOntologyAxiomsSignature(type, ontologyId);
        }
    }
}
