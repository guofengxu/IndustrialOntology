package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubObjectPropertyAxiomsBySubPropertyIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class SubObjectPropertyAxiomsBySubPropertyIndex implements org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public SubObjectPropertyAxiomsBySubPropertyIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLSubObjectPropertyOfAxiom> getSubPropertyOfAxioms(@Nonnull OWLObjectProperty property,
                                                                      @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(property);
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_OBJECT_PROPERTY, ontologyId)
                         .filter(ax -> ax.getSubProperty().equals(property));
    }
}
