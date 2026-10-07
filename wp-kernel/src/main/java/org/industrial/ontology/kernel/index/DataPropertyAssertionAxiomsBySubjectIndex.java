package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DataPropertyAssertionAxiomsBySubjectIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-12
 */
public class DataPropertyAssertionAxiomsBySubjectIndex implements org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex,
        UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLIndividual, OWLDataPropertyAssertionAxiom> index;

    public DataPropertyAssertionAxiomsBySubjectIndex() {
        index = AxiomMultimapIndex.create(OWLDataPropertyAssertionAxiom.class,
                                          OWLDataPropertyAssertionAxiom::getSubject);
    }

    @Nonnull
    @Override
    public Stream<OWLDataPropertyAssertionAxiom> getDataPropertyAssertions(@Nonnull OWLIndividual individual,
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
