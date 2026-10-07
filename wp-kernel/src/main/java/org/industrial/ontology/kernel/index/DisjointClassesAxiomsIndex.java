package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DisjointClassesAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-22
 */
public class DisjointClassesAxiomsIndex implements org.industrial.ontology.kernel.api.index.DisjointClassesAxiomsIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLClassExpression, OWLDisjointClassesAxiom> index;

    public DisjointClassesAxiomsIndex() {
        index = AxiomMultimapIndex.createWithNaryKeyValueExtractor(OWLDisjointClassesAxiom.class,
                                                                   OWLDisjointClassesAxiom::getClassExpressions);

        index.setLazy(true);
    }

    @Nonnull
    @Override
    public Stream<OWLDisjointClassesAxiom> getDisjointClassesAxioms(@Nonnull OWLClass cls, OWLOntologyID ontologyId) {
        checkNotNull(cls);
        checkNotNull(ontologyId);
        return index.getAxioms(cls, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
