package org.industrial.ontology.app.hierarchy;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.hierarchy.DropType;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.industrial.ontology.domain.hierarchy.MoveHierarchyNodeRequest;
import org.industrial.ontology.domain.hierarchy.Path;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.kernel.api.hierarchy.HierarchyProvider;
import org.industrial.ontology.kernel.api.pagination.PageCollector;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * The class and property hierarchies of a project (docs/01 §5.1, stage S7): the legacy
 * {@code GetHierarchyRoots/Children/Siblings/PathsToRoot} and {@code MoveHierarchyNode} handlers. Reading needs
 * {@code ViewProject}, moving {@code EditOntology}, as before.
 * <p>
 * Nodes say whether they are leaves. Children are sorted by display name ignoring case, without deprecated
 * entities directly below a top entity, as before; the legacy handler always used pages of 2000 children, whereas
 * the page size is now the caller's.
 */
public class HierarchyService {

    private static final Map<HierarchyId, EntityType<?>> ENTITY_TYPES = Map.of(
            HierarchyId.CLASS_HIERARCHY, EntityType.CLASS,
            HierarchyId.OBJECT_PROPERTY_HIERARCHY, EntityType.OBJECT_PROPERTY,
            HierarchyId.DATA_PROPERTY_HIERARCHY, EntityType.DATA_PROPERTY,
            HierarchyId.ANNOTATION_PROPERTY_HIERARCHY, EntityType.ANNOTATION_PROPERTY);

    private final ProjectContexts projects;

    public HierarchyService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * The roots of the hierarchy, by display name.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for an unknown hierarchy
     */
    @Nonnull
    public List<GraphNode<EntityNode>> getRoots(@Nonnull UserId caller,
                                                @Nonnull ProjectId projectId,
                                                @Nonnull HierarchyId hierarchyId) {
        requireHierarchy(hierarchyId);
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var hierarchy = hierarchy(context, hierarchyId);
            var renderer = context.components().graphNodeRenderer();
            return hierarchy.getRoots()
                            .stream()
                            .map(root -> renderer.toGraphNode(root, hierarchy))
                            .sorted(Comparator.comparing(node -> node.userObject().getBrowserText()))
                            .toList();
        });
    }

    /**
     * The parent's node and a page of its children.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for an unknown hierarchy
     */
    @Nonnull
    public Children getChildren(@Nonnull UserId caller,
                                @Nonnull ProjectId projectId,
                                @Nonnull HierarchyId hierarchyId,
                                @Nonnull OWLEntity parent,
                                @Nonnull PageRequest pageRequest) {
        requireHierarchy(hierarchyId);
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var hierarchy = hierarchy(context, hierarchyId);
            var renderer = context.components().graphNodeRenderer();
            var dictionary = context.dictionary();
            var deprecated = context.deprecatedEntityChecker();
            var children = hierarchy.getChildren(parent)
                                    .stream()
                                    .filter(child -> !(parent.isTopEntity() && deprecated.isDeprecated(child)))
                                    .sorted(Comparator.comparing(dictionary::getShortForm,
                                                                 String.CASE_INSENSITIVE_ORDER))
                                    .collect(PageCollector.toPage(pageRequest.getPageNumber(),
                                                                  pageRequest.getPageSize()))
                                    .map(page -> page.transform(child -> renderer.toGraphNode(child, hierarchy)))
                                    .orElse(Page.emptyPage());
            return new Children(renderer.toGraphNode(parent, hierarchy), children);
        });
    }

    /**
     * The other children of the entity's parents, by display name, a page at a time.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for an unknown hierarchy
     */
    @Nonnull
    public Page<GraphNode<EntityNode>> getSiblings(@Nonnull UserId caller,
                                                   @Nonnull ProjectId projectId,
                                                   @Nonnull HierarchyId hierarchyId,
                                                   @Nonnull OWLEntity entity,
                                                   @Nonnull PageRequest pageRequest) {
        requireHierarchy(hierarchyId);
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var hierarchy = hierarchy(context, hierarchyId);
            var renderer = context.components().graphNodeRenderer();
            return hierarchy.getParents(entity)
                            .stream()
                            .flatMap(parent -> hierarchy.getChildren(parent).stream())
                            .filter(sibling -> !sibling.equals(entity))
                            .distinct()
                            .sorted(Comparator.comparing(context.dictionary()::getShortForm))
                            .collect(PageCollector.toPage(pageRequest.getPageNumber(), pageRequest.getPageSize()))
                            .orElse(Page.emptyPage())
                            .transform(sibling -> renderer.toGraphNode(sibling, hierarchy));
        });
    }

    /**
     * The paths from a root down to the entity, which has one path for each way it is reached.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for an unknown hierarchy
     */
    @Nonnull
    public List<Path<GraphNode<EntityNode>>> getPathsToRoot(@Nonnull UserId caller,
                                                            @Nonnull ProjectId projectId,
                                                            @Nonnull HierarchyId hierarchyId,
                                                            @Nonnull OWLEntity entity) {
        requireHierarchy(hierarchyId);
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var hierarchy = hierarchy(context, hierarchyId);
            var renderer = context.components().graphNodeRenderer();
            return hierarchy.getPathsToRoot(entity)
                            .stream()
                            .map(path -> Path.of(path.stream()
                                                     .map(element -> renderer.toGraphNode(element, hierarchy))
                                                     .toList()))
                            .toList();
        });
    }

    /**
     * Moves the entity from one parent to another in one revision by the caller, or with {@link DropType#COPY} adds
     * the new parent. A missing parent is the root of the hierarchy. Nothing changes when the entity is placed below
     * its old parent by an equivalent classes axiom.
     *
     * @param commitMessage describes the revision; without one it says what moved where
     * @return whether anything changed
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@code INVALID_REQUEST} for an unknown hierarchy, or an entity or parent that
     *                                   is not of the hierarchy's type
     */
    @Nonnull
    public ChangeOutcome<Boolean> move(@Nonnull UserId caller,
                                       @Nonnull ProjectId projectId,
                                       @Nonnull HierarchyId hierarchyId,
                                       @Nonnull OWLEntity entity,
                                       @Nullable OWLEntity fromParent,
                                       @Nullable OWLEntity toParent,
                                       @Nonnull DropType dropType,
                                       @Nullable String commitMessage) {
        var type = requireHierarchy(hierarchyId);
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        if (Stream.of(entity, fromParent, toParent)
                  .anyMatch(node -> node != null && !node.getEntityType().equals(type))) {
            throw WpException.invalidRequest("The " + hierarchyId.getId() + " hierarchy only has "
                                                     + type.getPluralPrintName().toLowerCase());
        }
        if (entity.isBuiltIn()) {
            throw WpException.invalidRequest("Built-in entities such as " + entity.getIRI() + " cannot be moved");
        }
        var request = ProjectContexts.read(context, ctx -> {
            var renderer = ctx.entityNodeRenderer();
            var fromPath = fromParent == null
                    ? Path.of(renderer.render(entity))
                    : Path.of(renderer.render(fromParent), renderer.render(entity));
            Path<EntityNode> toPath = toParent == null ? Path.emptyPath() : Path.of(renderer.render(toParent));
            return new MoveHierarchyNodeRequest(projectId, hierarchyId, fromPath, toPath, dropType);
        });
        var generator = GuardedChange.of(context.components().moveEntityChangeListGeneratorFactory().create(request))
                                     .withCommitMessage(commitMessage);
        return ProjectContexts.apply(context, caller, generator);
    }

    private static EntityType<?> requireHierarchy(HierarchyId hierarchyId) {
        var type = ENTITY_TYPES.get(hierarchyId);
        if (type == null) {
            throw WpException.invalidRequest("There is no " + hierarchyId.getId() + " hierarchy");
        }
        return type;
    }

    private static HierarchyProvider<OWLEntity> hierarchy(ProjectContext context, HierarchyId hierarchyId) {
        return context.components()
                      .hierarchyProviderMapper()
                      .getHierarchyProvider(hierarchyId)
                      .orElseThrow(() -> WpException.invalidRequest("There is no " + hierarchyId.getId()
                                                                            + " hierarchy"));
    }

    /**
     * A node and a page of its children.
     */
    public record Children(@Nonnull GraphNode<EntityNode> parent, @Nonnull Page<GraphNode<EntityNode>> children) {
    }
}
