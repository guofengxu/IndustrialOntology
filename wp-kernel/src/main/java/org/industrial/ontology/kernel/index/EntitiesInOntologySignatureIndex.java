package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.OntologyAnnotationsSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsSignatureIndex;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EntitiesInOntologySignatureIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
public class EntitiesInOntologySignatureIndex implements org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureIndex {

    @Nonnull
    private final OntologyAxiomsSignatureIndex ontologyAxiomsSignatureIndex;

    @Nonnull
    private final OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex;

    public EntitiesInOntologySignatureIndex(@Nonnull OntologyAxiomsSignatureIndex ontologyAxiomsSignatureIndex,
                                                @Nonnull OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex) {
        this.ontologyAxiomsSignatureIndex = checkNotNull(ontologyAxiomsSignatureIndex);
        this.ontologyAnnotationsSignatureIndex = checkNotNull(ontologyAnnotationsSignatureIndex);
    }


    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity entity, @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(entity);
        var inAxiomsSignature = ontologyAxiomsSignatureIndex.containsEntityInOntologyAxiomsSignature(entity, ontologyId);
        if(inAxiomsSignature) {
            return true;
        }
        if(entity.isOWLAnnotationProperty()) {
            return ontologyAnnotationsSignatureIndex.containsEntityInOntologyAnnotationsSignature(entity, ontologyId);
        }
        return false;

    }
}
