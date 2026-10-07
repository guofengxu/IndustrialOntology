package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.OntologySignatureIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class OntologySignatureIndex implements org.industrial.ontology.kernel.api.index.OntologySignatureIndex, DependentIndex {

    @Nonnull
    private final AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex;

    public OntologySignatureIndex(@Nonnull AxiomsByEntityReferenceIndex axiomsByEntityReferenceIndex) {
        this.axiomsByEntityReferenceIndex = checkNotNull(axiomsByEntityReferenceIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByEntityReferenceIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLEntity> getEntitiesInSignature(@Nonnull OWLOntologyID ontologyID) {
        checkNotNull(ontologyID);
        var streamsBuilder = Stream.<Stream<? extends OWLEntity>>builder();
        EntityType.values().forEach(entityType -> {
            var sig = axiomsByEntityReferenceIndex.getOntologyAxiomsSignature(entityType,
                                                                    ontologyID);
            streamsBuilder.add(sig);
        });
        return streamsBuilder.build().flatMap(s -> s);
    }
}
