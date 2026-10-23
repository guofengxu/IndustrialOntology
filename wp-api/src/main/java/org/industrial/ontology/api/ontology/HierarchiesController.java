package org.industrial.ontology.api.ontology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.ontology.OntologyJson.ChangeOutcomeDto;
import org.industrial.ontology.api.ontology.OntologyJson.GraphNodeDto;
import org.industrial.ontology.api.ontology.OntologyJson.PageDto;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.hierarchy.HierarchyService;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.hierarchy.DropType;
import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.api.ontology.OntologyParams.entity;
import static org.industrial.ontology.api.ontology.OntologyParams.page;
import static org.industrial.ontology.api.ontology.OntologyParams.required;

/**
 * A project's class and property hierarchies (docs/02 §4.2). {@code hierarchyId} is {@code Class},
 * {@code ObjectProperty}, {@code DataProperty} or {@code AnnotationProperty} (case ignored); the entities named by
 * {@code iri} are of the hierarchy's type. The service checks the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/hierarchies/{hierarchyId}")
@Tag(name = "Hierarchies", description = "Roots, children, siblings, paths to the root and moving nodes")
public class HierarchiesController {

    /** The legacy handler's page of children. */
    private static final int MAX_CHILDREN_PAGE_SIZE = 2000;

    private static final Map<String, HierarchyId> HIERARCHIES = Map.of(
            "class", HierarchyId.CLASS_HIERARCHY,
            "objectproperty", HierarchyId.OBJECT_PROPERTY_HIERARCHY,
            "dataproperty", HierarchyId.DATA_PROPERTY_HIERARCHY,
            "annotationproperty", HierarchyId.ANNOTATION_PROPERTY_HIERARCHY);

    private static final Map<HierarchyId, EntityType<?>> TYPES = Map.of(
            HierarchyId.CLASS_HIERARCHY, EntityType.CLASS,
            HierarchyId.OBJECT_PROPERTY_HIERARCHY, EntityType.OBJECT_PROPERTY,
            HierarchyId.DATA_PROPERTY_HIERARCHY, EntityType.DATA_PROPERTY,
            HierarchyId.ANNOTATION_PROPERTY_HIERARCHY, EntityType.ANNOTATION_PROPERTY);

    private final HierarchyService hierarchyService;

    public HierarchiesController(HierarchyService hierarchyService) {
        this.hierarchyService = checkNotNull(hierarchyService);
    }

    @Operation(summary = "The roots of the hierarchy (permission ViewProject)")
    @GetMapping("/roots")
    public List<GraphNodeDto> roots(@Caller UserId caller,
                                    @PathVariable String projectId,
                                    @PathVariable String hierarchyId) {
        return hierarchyService.getRoots(caller, ProjectIds.parse(projectId), hierarchy(hierarchyId))
                               .stream()
                               .map(GraphNodeDto::of)
                               .toList();
    }

    @Operation(summary = "The node and a page of its children, by display name (permission ViewProject)")
    @GetMapping("/children")
    public ChildrenDto children(@Caller UserId caller,
                                @PathVariable String projectId,
                                @PathVariable String hierarchyId,
                                @RequestParam(required = false) String iri,
                                @RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "2000") int size) {
        var hierarchy = hierarchy(hierarchyId);
        var children = hierarchyService.getChildren(caller, ProjectIds.parse(projectId), hierarchy,
                                                    entity(iri, TYPES.get(hierarchy)),
                                                    page(page, size, MAX_CHILDREN_PAGE_SIZE));
        return new ChildrenDto(GraphNodeDto.of(children.parent()),
                               PageDto.of(children.children(), GraphNodeDto::of));
    }

    @Operation(summary = "The other children of the entity's parents, a page at a time (permission ViewProject)")
    @GetMapping("/siblings")
    public PageDto<GraphNodeDto> siblings(@Caller UserId caller,
                                          @PathVariable String projectId,
                                          @PathVariable String hierarchyId,
                                          @RequestParam(required = false) String iri,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "2000") int size) {
        var hierarchy = hierarchy(hierarchyId);
        return PageDto.of(hierarchyService.getSiblings(caller, ProjectIds.parse(projectId), hierarchy,
                                                       entity(iri, TYPES.get(hierarchy)),
                                                       page(page, size, MAX_CHILDREN_PAGE_SIZE)),
                          GraphNodeDto::of);
    }

    @Operation(summary = "The paths from a root down to the entity (permission ViewProject)")
    @GetMapping("/paths-to-root")
    public List<List<GraphNodeDto>> pathsToRoot(@Caller UserId caller,
                                                @PathVariable String projectId,
                                                @PathVariable String hierarchyId,
                                                @RequestParam(required = false) String iri) {
        var hierarchy = hierarchy(hierarchyId);
        return hierarchyService.getPathsToRoot(caller, ProjectIds.parse(projectId), hierarchy,
                                               entity(iri, TYPES.get(hierarchy)))
                               .stream()
                               .map(path -> path.asList().stream().map(GraphNodeDto::of).toList())
                               .toList();
    }

    @Operation(summary = "Moves the entity from one parent to another, or adds the parent (permission "
            + "EditOntology)",
               description = "dropType is MOVE or ADD (also COPY); a missing parent is the root.")
    @PostMapping("/move")
    public ChangeOutcomeDto<MovedDto> move(@Caller UserId caller,
                                           @PathVariable String projectId,
                                           @PathVariable String hierarchyId,
                                           @RequestBody MoveRequest request) {
        var dropType = switch (required(request.dropType(), "dropType").strip().toUpperCase(Locale.ROOT)) {
            case "MOVE" -> DropType.MOVE;
            case "ADD", "COPY" -> DropType.COPY;
            default -> throw WpException.invalidRequest("dropType is MOVE or ADD");
        };
        var outcome = hierarchyService.move(caller,
                                            ProjectIds.parse(projectId),
                                            hierarchy(hierarchyId),
                                            required(request.entity(), "entity"),
                                            request.fromParent(),
                                            request.toParent(),
                                            dropType,
                                            request.commitMessage());
        return ChangeOutcomeDto.of(outcome, MovedDto::new);
    }

    private static HierarchyId hierarchy(String hierarchyId) {
        var hierarchy = HIERARCHIES.get(hierarchyId.toLowerCase(Locale.ROOT));
        if (hierarchy == null) {
            throw WpException.invalidRequest("hierarchyId is Class, ObjectProperty, DataProperty or "
                                                     + "AnnotationProperty");
        }
        return hierarchy;
    }

    public record ChildrenDto(GraphNodeDto parent, PageDto<GraphNodeDto> children) {
    }

    public record MoveRequest(OWLEntity entity,
                              OWLEntity fromParent,
                              OWLEntity toParent,
                              String dropType,
                              String commitMessage) {
    }

    /**
     * Whether the move changed anything; nothing changes when an equivalent classes axiom places the entity below
     * its old parent.
     */
    public record MovedDto(boolean moved) {
    }
}
