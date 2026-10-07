package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ClassAssertionAxiomsByClassIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-19
 */
public class ClassAssertionAxiomsByClassIndex implements org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLClassExpression, OWLClassAssertionAxiom> index;

    public ClassAssertionAxiomsByClassIndex() {
        index = AxiomMultimapIndex.create(OWLClassAssertionAxiom.class,
                                          OWLClassAssertionAxiom::getClassExpression);
        index.setLazy(true);
    }

    @Nonnull
    @Override
    public Stream<OWLClassAssertionAxiom> getClassAssertionAxioms(@Nonnull OWLClass cls,
                                                                  @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(cls);
        return index.getAxioms(cls, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
