package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubAnnotationPropertyAxiomsBySubPropertyIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class SubAnnotationPropertyAxiomsBySubPropertyIndex implements org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public SubAnnotationPropertyAxiomsBySubPropertyIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLSubAnnotationPropertyOfAxiom> getSubPropertyOfAxioms(@Nonnull OWLAnnotationProperty property,
                                                                          @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(property);
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_ANNOTATION_PROPERTY_OF, ontologyId)
                                .filter(ax -> ax.getSubProperty()
                                                .equals(property));
    }
}
