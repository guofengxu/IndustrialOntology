package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DifferentIndividualsAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-24
 */
public class DifferentIndividualsAxiomsIndex implements org.industrial.ontology.kernel.api.index.DifferentIndividualsAxiomsIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLIndividual, OWLDifferentIndividualsAxiom> index;

    public DifferentIndividualsAxiomsIndex() {
        this.index = AxiomMultimapIndex.createWithNaryKeyValueExtractor(
                OWLDifferentIndividualsAxiom.class,
                OWLDifferentIndividualsAxiom::getIndividuals);
    }

    @Nonnull
    @Override
    public Stream<OWLDifferentIndividualsAxiom> getDifferentIndividualsAxioms(@Nonnull OWLIndividual individual,
                                                                              @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(individual);
        return index.getAxioms(individual, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
