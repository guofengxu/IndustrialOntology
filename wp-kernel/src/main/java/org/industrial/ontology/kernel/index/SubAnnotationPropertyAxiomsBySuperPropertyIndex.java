package org.industrial.ontology.kernel.index;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubAnnotationPropertyAxiomsBySuperPropertyIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-17
 */
public class SubAnnotationPropertyAxiomsBySuperPropertyIndex implements org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySuperPropertyIndex, UpdatableIndex {

    @Nonnull
    private final AxiomMultimapIndex<OWLAnnotationProperty, OWLSubAnnotationPropertyOfAxiom> index;

    public SubAnnotationPropertyAxiomsBySuperPropertyIndex() {
        index = AxiomMultimapIndex.create(OWLSubAnnotationPropertyOfAxiom.class,
                                          OWLSubAnnotationPropertyOfAxiom::getSuperProperty);
    }

    @Nonnull
    @Override
    public Stream<OWLSubAnnotationPropertyOfAxiom> getAxiomsForSuperProperty(@Nonnull OWLAnnotationProperty property,
                                                                             @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(property);
        return index.getAxioms(property, ontologyId);
    }

    @Override
    public void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
        index.applyChanges(changes);
    }
}
