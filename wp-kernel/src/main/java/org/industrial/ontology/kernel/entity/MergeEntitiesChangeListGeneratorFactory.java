package org.industrial.ontology.kernel.entity;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link MergeEntitiesChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.entity.MergeEntitiesChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class MergeEntitiesChangeListGeneratorFactory {

    private final Supplier<ProjectId> projectId;

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<EntityDiscussionThreadRepository> discussionThreadRepository;

    private final Supplier<EntityRenamer> entityRenamer;

    private final Supplier<DefaultOntologyIdManager> defaultOntologyIdManager;

    private final Supplier<ProjectOntologiesIndex> projectOntologies;

    private final Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertions;

    public MergeEntitiesChangeListGeneratorFactory(Supplier<ProjectId> projectId,
            Supplier<OWLDataFactory> dataFactory,
            Supplier<EntityDiscussionThreadRepository> discussionThreadRepository,
            Supplier<EntityRenamer> entityRenamer,
            Supplier<DefaultOntologyIdManager> defaultOntologyIdManager,
            Supplier<ProjectOntologiesIndex> projectOntologies,
            Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertions) {
        this.projectId = java.util.Objects.requireNonNull(projectId);
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.discussionThreadRepository = java.util.Objects.requireNonNull(discussionThreadRepository);
        this.entityRenamer = java.util.Objects.requireNonNull(entityRenamer);
        this.defaultOntologyIdManager = java.util.Objects.requireNonNull(defaultOntologyIdManager);
        this.projectOntologies = java.util.Objects.requireNonNull(projectOntologies);
        this.annotationAssertions = java.util.Objects.requireNonNull(annotationAssertions);
    }

    public MergeEntitiesChangeListGenerator create(@Nonnull ImmutableSet<OWLEntity> sourceEntities, @Nonnull OWLEntity targetEntity, @Nonnull MergedEntityTreatment treatment, @Nonnull String commitMessage) {
        return new MergeEntitiesChangeListGenerator(sourceEntities, targetEntity, treatment, commitMessage, projectId.get(), dataFactory.get(), discussionThreadRepository.get(), entityRenamer.get(), defaultOntologyIdManager.get(), projectOntologies.get(), annotationAssertions.get());
    }
}
