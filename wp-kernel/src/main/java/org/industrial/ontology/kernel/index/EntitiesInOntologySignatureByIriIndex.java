package org.industrial.ontology.kernel.index;



import com.google.common.collect.Streams;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.OntologyAnnotationsSignatureIndex;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EntitiesInOntologySignatureByIriIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-09-05
 */
public class EntitiesInOntologySignatureByIriIndex implements org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureByIriIndex, DependentIndex {

    @Nonnull
    private final AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex;

    @Nonnull
    private final OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex;

    public EntitiesInOntologySignatureByIriIndex(@Nonnull AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex,
                                                     @Nonnull OntologyAnnotationsSignatureIndex ontologyAnnotationsSignatureIndex) {
        this.axiomsByEntityReferenceIndex = axiomsByEntityReferenceIndex;
        this.ontologyAnnotationsSignatureIndex = ontologyAnnotationsSignatureIndex;
    }

    @Nonnull
    @Override
    public Stream<OWLEntity> getEntitiesInSignature(@Nonnull IRI iri, @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(iri);
        checkNotNull(ontologyId);
        var axiomsSignature = axiomsByEntityReferenceIndex.getEntitiesInSignatureWithIri(iri, ontologyId);
        var ontologyAnnotationsSignature = ontologyAnnotationsSignatureIndex.getOntologyAnnotationsSignature(ontologyId)
                .filter(entity -> entity.getIRI().equals(iri));
        return Streams.concat(axiomsSignature, ontologyAnnotationsSignature);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByEntityReferenceIndex);
    }
}
