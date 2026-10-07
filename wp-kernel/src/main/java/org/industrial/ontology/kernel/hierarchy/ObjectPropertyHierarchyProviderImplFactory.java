package org.industrial.ontology.kernel.hierarchy;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.OntologySignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ObjectPropertyHierarchyProvider}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ObjectPropertyHierarchyProviderImplFactory} (generated in the legacy build).
 */
public final class ObjectPropertyHierarchyProviderImplFactory {

    private final Supplier<ProjectId> projectId;

    private final Supplier<OWLObjectProperty> root;

    private final Supplier<EntitiesInProjectSignatureIndex> entitiesInProjectSignatureIndex;

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    private final Supplier<OntologySignatureByTypeIndex> ontologySignatureByTypeIndex;

    private final Supplier<SubObjectPropertyAxiomsBySubPropertyIndex> subObjectPropertyAxiomsBySubPropertyIndex;

    private final Supplier<AxiomsByTypeIndex> axiomsByTypeIndex;

    public ObjectPropertyHierarchyProviderImplFactory(Supplier<ProjectId> projectId,
            Supplier<OWLObjectProperty> root,
            Supplier<EntitiesInProjectSignatureIndex> entitiesInProjectSignatureIndex,
            Supplier<ProjectOntologiesIndex> projectOntologiesIndex,
            Supplier<OntologySignatureByTypeIndex> ontologySignatureByTypeIndex,
            Supplier<SubObjectPropertyAxiomsBySubPropertyIndex> subObjectPropertyAxiomsBySubPropertyIndex,
            Supplier<AxiomsByTypeIndex> axiomsByTypeIndex) {
        this.projectId = java.util.Objects.requireNonNull(projectId);
        this.root = java.util.Objects.requireNonNull(root);
        this.entitiesInProjectSignatureIndex = java.util.Objects.requireNonNull(entitiesInProjectSignatureIndex);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.ontologySignatureByTypeIndex = java.util.Objects.requireNonNull(ontologySignatureByTypeIndex);
        this.subObjectPropertyAxiomsBySubPropertyIndex = java.util.Objects.requireNonNull(subObjectPropertyAxiomsBySubPropertyIndex);
        this.axiomsByTypeIndex = java.util.Objects.requireNonNull(axiomsByTypeIndex);
    }

    public ObjectPropertyHierarchyProvider create() {
        return new ObjectPropertyHierarchyProvider(projectId.get(), root.get(), entitiesInProjectSignatureIndex.get(), projectOntologiesIndex.get(), ontologySignatureByTypeIndex.get(), subObjectPropertyAxiomsBySubPropertyIndex.get(), axiomsByTypeIndex.get());
    }
}
