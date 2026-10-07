package org.industrial.ontology.kernel.index;



import com.google.common.collect.Streams;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.AnnotationAxiomsByIriReferenceIndex;

import org.industrial.ontology.kernel.api.index.AxiomsByEntityReferenceIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AxiomsByReferenceIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-06
 */
public class AxiomsByReferenceIndex implements org.industrial.ontology.kernel.api.index.AxiomsByReferenceIndex, DependentIndex {

    @Nonnull
    private final AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex;

    @Nonnull
    private final AnnotationAxiomsByIriReferenceIndex axiomsByIriReferenceIndex;

    public AxiomsByReferenceIndex(@Nonnull AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex,
                                      @Nonnull AnnotationAxiomsByIriReferenceIndex axiomsByIriReferenceIndex) {
        this.axiomsByEntityReferenceIndex = checkNotNull(axiomsByEntityReferenceIndex);
        this.axiomsByIriReferenceIndex = checkNotNull(axiomsByIriReferenceIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByIriReferenceIndex, axiomsByEntityReferenceIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLAxiom> getReferencingAxioms(@Nonnull Collection<OWLEntity> entities,
                                                 @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(entities);
        return entities.stream().flatMap(entity -> getReferencingAxioms(entity, ontologyId));
    }


    @Nonnull
    private Stream<OWLAxiom> getReferencingAxioms(OWLEntity entity,
                                                  OWLOntologyID ontologyId) {
        // Combine both entity in signature and IRI mentions in annotation axioms
        var entityReferencingAxioms = axiomsByEntityReferenceIndex.getReferencingAxioms(entity, ontologyId);
        var iriReferencingAxioms = axiomsByIriReferenceIndex.getReferencingAxioms(entity.getIRI(), ontologyId);
        return Streams.concat(entityReferencingAxioms, iriReferencingAxioms);
    }
}
