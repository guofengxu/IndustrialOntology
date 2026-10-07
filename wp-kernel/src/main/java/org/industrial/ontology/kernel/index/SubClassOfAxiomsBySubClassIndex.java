package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubClassOfAxiomsBySubClassIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
public class SubClassOfAxiomsBySubClassIndex implements org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLClass, OWLSubClassOfAxiom> index;

    public SubClassOfAxiomsBySubClassIndex() {
        index = AxiomMultimapIndex.create(OWLSubClassOfAxiom.class,
                                          this::extractSubClassFromAxiom);

    }

    @Nullable
    private OWLClass extractSubClassFromAxiom(@Nonnull OWLSubClassOfAxiom ax) {
        var subClass = ax.getSubClass();
        if(subClass.isNamed()) {
            return subClass.asOWLClass();
        }
        else {
            return null;
        }
    }

    @Override
    public Stream<OWLSubClassOfAxiom> getSubClassOfAxiomsForSubClass(@Nonnull OWLClass subClass,
                                                                     @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(subClass);
        checkNotNull(ontologyId);
        return index.getAxioms(subClass, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
