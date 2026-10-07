package org.industrial.ontology.kernel.hierarchy;

import org.industrial.ontology.kernel.msg.MessageFormatter;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.domain.hierarchy.MoveHierarchyNodeRequest;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SubAnnotationPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.api.index.SubDataPropertyAxiomsBySubPropertyIndex;
import org.industrial.ontology.kernel.api.index.SubObjectPropertyAxiomsBySubPropertyIndex;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link MoveEntityChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.MoveEntityChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class MoveEntityChangeListGeneratorFactory {

    private final OWLDataFactory dataFactory;

    private final MessageFormatter msg;

    private final ProjectOntologiesIndex projectOntologiesIndex;

    private final DefaultOntologyIdManager defaultOntologyIdManager;

    private final EquivalentClassesAxiomsIndex equivalentClassesAxiomsIndex;

    private final SubClassOfAxiomsBySubClassIndex subClassOfAxiomsIndex;

    private final SubObjectPropertyAxiomsBySubPropertyIndex subObjectPropertyOfAxiomsIndex;

    private final SubDataPropertyAxiomsBySubPropertyIndex subDataPropertyOfAxiomsIndex;

    private final SubAnnotationPropertyAxiomsBySubPropertyIndex subAnnotationPropertyOfAxiomsIndex;

    public MoveEntityChangeListGeneratorFactory(OWLDataFactory dataFactory,
            MessageFormatter msg,
            ProjectOntologiesIndex projectOntologiesIndex,
            DefaultOntologyIdManager defaultOntologyIdManager,
            EquivalentClassesAxiomsIndex equivalentClassesAxiomsIndex,
            SubClassOfAxiomsBySubClassIndex subClassOfAxiomsIndex,
            SubObjectPropertyAxiomsBySubPropertyIndex subObjectPropertyOfAxiomsIndex,
            SubDataPropertyAxiomsBySubPropertyIndex subDataPropertyOfAxiomsIndex,
            SubAnnotationPropertyAxiomsBySubPropertyIndex subAnnotationPropertyOfAxiomsIndex) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.msg = java.util.Objects.requireNonNull(msg);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.defaultOntologyIdManager = java.util.Objects.requireNonNull(defaultOntologyIdManager);
        this.equivalentClassesAxiomsIndex = java.util.Objects.requireNonNull(equivalentClassesAxiomsIndex);
        this.subClassOfAxiomsIndex = java.util.Objects.requireNonNull(subClassOfAxiomsIndex);
        this.subObjectPropertyOfAxiomsIndex = java.util.Objects.requireNonNull(subObjectPropertyOfAxiomsIndex);
        this.subDataPropertyOfAxiomsIndex = java.util.Objects.requireNonNull(subDataPropertyOfAxiomsIndex);
        this.subAnnotationPropertyOfAxiomsIndex = java.util.Objects.requireNonNull(subAnnotationPropertyOfAxiomsIndex);
    }

    public MoveEntityChangeListGenerator create(@Nonnull MoveHierarchyNodeRequest action) {
        return new MoveEntityChangeListGenerator(action, dataFactory, msg, projectOntologiesIndex, defaultOntologyIdManager, equivalentClassesAxiomsIndex, subClassOfAxiomsIndex, subObjectPropertyOfAxiomsIndex, subDataPropertyOfAxiomsIndex, subAnnotationPropertyOfAxiomsIndex);
    }
}
