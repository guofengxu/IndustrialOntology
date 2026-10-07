package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ClassAssertionAxiomsByIndividualIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
public class ClassAssertionAxiomsByIndividualIndex implements org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLIndividual, OWLClassAssertionAxiom> index;

    public ClassAssertionAxiomsByIndividualIndex() {
        index = AxiomMultimapIndex.create(OWLClassAssertionAxiom.class,
                                          OWLClassAssertionAxiom::getIndividual);

        index.setLazy(true);
    }

    @Override
    public Stream<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull OWLIndividual individual,
                                                                  @Nonnull OWLOntologyID ontologyID) {
        checkNotNull(individual);
        checkNotNull(ontologyID);
        return index.getAxioms(individual, ontologyID);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
