package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SameIndividualAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public class SameIndividualAxiomsIndex implements org.industrial.ontology.kernel.api.index.SameIndividualAxiomsIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLIndividual, OWLSameIndividualAxiom> index;

    public SameIndividualAxiomsIndex() {
        index = AxiomMultimapIndex.createWithNaryKeyValueExtractor(OWLSameIndividualAxiom.class,
                                                                   OWLSameIndividualAxiom::getIndividuals);
    }

    @Nonnull
    @Override
    public Stream<OWLSameIndividualAxiom> getSameIndividualAxioms(@Nonnull OWLIndividual individual,
                                                                  @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(individual);
        checkNotNull(ontologyId);
        return index.getAxioms(individual, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
