package org.industrial.ontology.app.entity;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.kernel.api.index.IndividualsIndex;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * The individuals of a project, by type (docs/01 §5.1, stage S7): the legacy {@code GetIndividuals} and
 * {@code GetIndividualsPageContainingIndividual} handlers. Both need {@code ViewProject}; the legacy handler of the
 * second checked nothing.
 */
public class IndividualsService {

    /** The page size of the page that contains an individual, as before. */
    static final int PAGE_CONTAINING_SIZE = 200;

    private final ProjectContexts projects;

    public IndividualsService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The individuals of the type (all of them without a type), directly or through subclasses, whose names
     * contain the filter, a page at a time.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public Individuals getIndividuals(@Nonnull UserId caller,
                                      @Nonnull ProjectId projectId,
                                      @Nullable OWLClass type,
                                      @Nonnull InstanceRetrievalMode mode,
                                      @Nonnull String filter,
                                      @Nonnull PageRequest pageRequest) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var queriedType = type != null ? type : context.dataFactory().getOWLThing();
            var result = context.indexes()
                                .get(IndividualsIndex.class)
                                .getIndividuals(queriedType, mode, filter, pageRequest);
            var renderer = context.entityNodeRenderer();
            var renderedType = Optional.ofNullable(type).map(context.rendering()::getClassData);
            return new Individuals(renderedType,
                                   result.getIndividuals().transform(renderer::render),
                                   result.getIndividualsCount());
        });
    }

    /**
     * The page of 200 individuals that contains the individual, among the individuals of the preferred type (or of
     * one of the individual's types) in the preferred mode, with the individual's types.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public PageContaining getPageContaining(@Nonnull UserId caller,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull OWLNamedIndividual individual,
                                            @Nullable OWLClass preferredType,
                                            @Nonnull InstanceRetrievalMode preferredMode) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var index = context.indexes().get(IndividualsIndex.class);
            var result = index.getIndividualsPageContaining(individual,
                                                            Optional.ofNullable(preferredType),
                                                            preferredMode,
                                                            PAGE_CONTAINING_SIZE);
            var renderer = context.entityNodeRenderer();
            var types = index.getTypes(individual)
                             .map(renderer::render)
                             .sorted(Comparator.comparing(EntityNode::getBrowserText,
                                                          String.CASE_INSENSITIVE_ORDER))
                             .toList();
            return new PageContaining(renderer.render(individual),
                                      result.getIndividuals().transform(renderer::render),
                                      renderer.render(result.getType()),
                                      result.getMode(),
                                      types);
        });
    }

    /**
     * A page of individuals.
     *
     * @param type  the type asked for, if any
     * @param count how many individuals match in all
     */
    public record Individuals(@Nonnull Optional<OWLClassData> type, @Nonnull Page<EntityNode> individuals, long count) {
    }

    /**
     * The page that contains an individual, and the type and mode that the page was taken from.
     */
    public record PageContaining(@Nonnull EntityNode individual,
                                 @Nonnull Page<EntityNode> individuals,
                                 @Nonnull EntityNode type,
                                 @Nonnull InstanceRetrievalMode mode,
                                 @Nonnull List<EntityNode> types) {
    }
}
