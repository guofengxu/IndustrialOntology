package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.OntologyAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
public class OntologyAxiomsIndex implements org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public OntologyAxiomsIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLAxiom> getAxioms(@Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        var streamsBuilder = Stream.<Stream<? extends OWLAxiom>>builder();
        AxiomType.AXIOM_TYPES.forEach(axiomType -> {
            streamsBuilder.add(axiomsByTypeIndex.getAxiomsByType(axiomType, ontologyId));
        });
        return streamsBuilder.build().flatMap(s -> s);
    }

    @Override
    public boolean containsAxiom(@Nonnull OWLAxiom axiom,
                                 @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(axiom);
        checkNotNull(ontologyId);
        return axiomsByTypeIndex.containsAxiom(axiom, ontologyId);
    }

    @Override
    public boolean containsAxiomIgnoreAnnotations(@Nonnull OWLAxiom axiom,
                                                  @Nonnull OWLOntologyID ontologyId) {
        return containsAxiom(axiom, ontologyId);
    }
}
