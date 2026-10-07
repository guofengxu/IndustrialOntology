package org.industrial.ontology.kernel.index;



import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.EquivalentObjectPropertiesAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-24
 */
public class EquivalentObjectPropertiesAxiomsIndex implements org.industrial.ontology.kernel.api.index.EquivalentObjectPropertiesAxiomsIndex, DependentIndex {

    @Nonnull
    private final AxiomsByTypeIndex axiomsByTypeIndex;

    public EquivalentObjectPropertiesAxiomsIndex(@Nonnull AxiomsByTypeIndex axiomsByTypeIndex) {
        // Use AxiomsByType because there are few ontologies with many EquivalentObjectProperties axioms
        this.axiomsByTypeIndex = checkNotNull(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(axiomsByTypeIndex);
    }

    @Nonnull
    @Override
    public Stream<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(@Nonnull OWLObjectProperty property,
                                                                                          @Nonnull OWLOntologyID ontologyId) {
        checkNotNull(ontologyId);
        checkNotNull(property);
        return axiomsByTypeIndex.getAxiomsByType(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, ontologyId)
                .filter(ax -> ax.getProperties().contains(property));
    }
}
