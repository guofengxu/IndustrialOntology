package org.industrial.ontology.kernel.change.bulkop;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link MoveClassesChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.bulkop.MoveClassesChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class MoveClassesChangeListGeneratorFactory {

    private final Supplier<ProjectOntologiesIndex> projectOntologies;

    private final Supplier<SubClassOfAxiomsBySubClassIndex> subClassAxiomIndex;

    private final Supplier<OWLDataFactory> dataFactory;

    public MoveClassesChangeListGeneratorFactory(Supplier<ProjectOntologiesIndex> projectOntologies,
            Supplier<SubClassOfAxiomsBySubClassIndex> subClassAxiomIndex,
            Supplier<OWLDataFactory> dataFactory) {
        this.projectOntologies = java.util.Objects.requireNonNull(projectOntologies);
        this.subClassAxiomIndex = java.util.Objects.requireNonNull(subClassAxiomIndex);
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
    }

    public MoveClassesChangeListGenerator create(@Nonnull ImmutableSet<OWLClass> childClasses, @Nonnull OWLClass targetParent, @Nonnull String commitMessage) {
        return new MoveClassesChangeListGenerator(childClasses, targetParent, commitMessage, projectOntologies.get(), subClassAxiomIndex.get(), dataFactory.get());
    }
}
