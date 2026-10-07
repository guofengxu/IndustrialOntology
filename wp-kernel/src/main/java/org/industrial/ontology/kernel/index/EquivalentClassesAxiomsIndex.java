package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EquivalentClassesAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public class EquivalentClassesAxiomsIndex implements org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLClass, OWLEquivalentClassesAxiom> index;

    public EquivalentClassesAxiomsIndex() {
        index = AxiomMultimapIndex.createWithNaryKeyValueExtractor(OWLEquivalentClassesAxiom.class,
                                                                   OWLEquivalentClassesAxiom::getNamedClasses);
    }

    @Nonnull
    @Override
    public Stream<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(@Nonnull OWLClass cls,
                                                                        @Nonnull OWLOntologyID ontologyID) {
        checkNotNull(cls);
        checkNotNull(ontologyID);
        return index.getAxioms(cls, ontologyID);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
