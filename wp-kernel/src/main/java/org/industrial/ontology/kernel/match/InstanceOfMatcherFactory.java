package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByClassIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.domain.match.HierarchyFilterType;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLClass;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link InstanceOfMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.InstanceOfMatcherFactory} (generated in the legacy build).
 */
public final class InstanceOfMatcherFactory {

    private final Supplier<ClassHierarchyProvider> hierarchyProvider;

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    private final Supplier<ClassAssertionAxiomsByClassIndex> classAssertionsByClass;

    private final Supplier<ProjectSignatureByTypeIndex> projectSignatureByType;

    public InstanceOfMatcherFactory(Supplier<ClassHierarchyProvider> hierarchyProvider,
            Supplier<ProjectOntologiesIndex> projectOntologiesIndex,
            Supplier<ClassAssertionAxiomsByClassIndex> classAssertionsByClass,
            Supplier<ProjectSignatureByTypeIndex> projectSignatureByType) {
        this.hierarchyProvider = java.util.Objects.requireNonNull(hierarchyProvider);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.classAssertionsByClass = java.util.Objects.requireNonNull(classAssertionsByClass);
        this.projectSignatureByType = java.util.Objects.requireNonNull(projectSignatureByType);
    }

    public InstanceOfMatcher create(@Nonnull OWLClass target, @Nonnull HierarchyFilterType filterType) {
        return new InstanceOfMatcher(hierarchyProvider.get(), projectOntologiesIndex.get(), classAssertionsByClass.get(), projectSignatureByType.get(), target, filterType);
    }
}
