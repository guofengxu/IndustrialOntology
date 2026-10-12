package org.industrial.ontology.api.ontology;

import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.ontology.OntologyJson.ChangeOutcomeDto;
import org.industrial.ontology.api.ontology.OntologyJson.EntityDataDto;
import org.industrial.ontology.api.ontology.OntologyJson.EntityNodeDto;
import org.industrial.ontology.api.ontology.OntologyJson.MatchDto;
import org.industrial.ontology.api.ontology.OntologyJson.PageDto;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.entity.BulkEditService;
import org.industrial.ontology.app.entity.EntityRenderingService;
import org.industrial.ontology.app.entity.EntityService;
import org.industrial.ontology.app.entity.IndividualsService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.search.SearchService;
import org.industrial.ontology.domain.bulkop.NewAnnotationData;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.MergedEntityTreatment;
import org.industrial.ontology.domain.individuals.InstanceRetrievalMode;
import org.industrial.ontology.domain.lang.LangTag;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.match.RootCriteria;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.industrial.ontology.domain.usage.UsageFilter;
import org.industrial.ontology.domain.usage.UsageReference;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.api.ontology.OntologyParams.constant;
import static org.industrial.ontology.api.ontology.OntologyParams.entity;
import static org.industrial.ontology.api.ontology.OntologyParams.entityTypes;
import static org.industrial.ontology.api.ontology.OntologyParams.page;
import static org.industrial.ontology.api.ontology.OntologyParams.required;

/**
 * A project's entities (docs/02 §4.1): finding, rendering, creating, deleting, merging and renaming them, bulk edits,
 * and individuals by type. Entities in bodies have the domain JSON {@code {"type":"owl:Class","iri":"…"}}; in the
 * query, an entity is {@code iri} with {@code type}. The services check the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Entities", description = "Lookup, search, rendering, usage, creation, deletion, merging, renaming, "
        + "bulk edits and individuals")
public class EntitiesController {

    private static final Set<EntityType<?>> ALL_TYPES = Set.copyOf(EntityType.values());

    private static final int MAX_PAGE_SIZE = 1000;

    private final EntityService entityService;

    private final EntityRenderingService renderingService;

    private final BulkEditService bulkEditService;

    private final IndividualsService individualsService;

    private final SearchService searchService;

    public EntitiesController(EntityService entityService,
                              EntityRenderingService renderingService,
                              BulkEditService bulkEditService,
                              IndividualsService individualsService,
                              SearchService searchService) {
        this.entityService = checkNotNull(entityService);
        this.renderingService = checkNotNull(renderingService);
        this.bulkEditService = checkNotNull(bulkEditService);
        this.individualsService = checkNotNull(individualsService);
        this.searchService = checkNotNull(searchService);
    }

    @Operation(summary = "Completes entity names: the entities whose display names contain the words of q "
            + "(permission ViewProject)")
    @GetMapping("/entities/lookup")
    public List<LookupResultDto> lookup(@Caller UserId caller,
                                        @PathVariable String projectId,
                                        @RequestParam(defaultValue = "") String q,
                                        @RequestParam(required = false) String types,
                                        @RequestParam(defaultValue = "20") int limit) {
        return entityService.lookupEntities(caller, ProjectIds.parse(projectId), q, entityTypes(types, ALL_TYPES),
                                            limit, null)
                            .stream()
                            .map(match -> new LookupResultDto(EntityNodeDto.of(match.entity()), MatchDto.of(match)))
                            .toList();
    }

    @Operation(summary = "Searches the entities by name, best matches first (permission ViewProject)",
               description = "langTags limits the languages searched; filters names search filters of the "
                       + "project that the entities must pass.")
    @PostMapping("/entities/search")
    public PageDto<SearchResultDto> search(@Caller UserId caller,
                                           @PathVariable String projectId,
                                           @RequestBody SearchRequest request) {
        var langTags = Objects.requireNonNullElse(request.langTags(), List.<String>of())
                              .stream()
                              .map(LangTag::get)
                              .collect(ImmutableSet.toImmutableSet());
        var filters = Objects.requireNonNullElse(request.filters(), List.<String>of())
                             .stream()
                             .map(EntitiesController::filterId)
                             .toList();
        var types = request.entityTypes() == null || request.entityTypes().isEmpty()
                ? ALL_TYPES
                : entityTypes(String.join(",", request.entityTypes()), ALL_TYPES);
        var results = searchService.search(caller,
                                           ProjectIds.parse(projectId),
                                           Objects.requireNonNullElse(request.searchString(), ""),
                                           types,
                                           LangTagFilter.get(langTags),
                                           filters,
                                           page(orOne(request.page()), orSize(request.size(), 50), MAX_PAGE_SIZE));
        return PageDto.of(results, result -> new SearchResultDto(EntityNodeDto.of(result.entity()),
                                                                 result.matches()
                                                                       .stream()
                                                                       .map(MatchDto::of)
                                                                       .toList()));
    }

    @Operation(summary = "The entities that match the criteria of the query builder (permission ViewProject)")
    @PostMapping("/entities/match")
    public PageDto<EntityNodeDto> match(@Caller UserId caller,
                                        @PathVariable String projectId,
                                        @RequestBody MatchRequest request) {
        var results = searchService.match(caller, ProjectIds.parse(projectId), required(request.criteria(),
                                                                                         "criteria"),
                                          page(orOne(request.page()), orSize(request.size(), 50), MAX_PAGE_SIZE));
        return PageDto.of(results, EntityNodeDto::of);
    }

    @Operation(summary = "The entity with its display name (permission ViewProject)")
    @GetMapping("/entities/rendering")
    public EntityDataDto rendering(@Caller UserId caller,
                                   @PathVariable String projectId,
                                   @RequestParam(required = false) String iri,
                                   @RequestParam(required = false) String type) {
        return EntityDataDto.of(renderingService.getRendering(caller, ProjectIds.parse(projectId),
                                                              entity(iri, type)));
    }

    @Operation(summary = "The entity's frame as HTML Manchester syntax (permission ViewProject)")
    @GetMapping("/entities/html")
    public EntityHtmlDto html(@Caller UserId caller,
                              @PathVariable String projectId,
                              @RequestParam(required = false) String iri,
                              @RequestParam(required = false) String type) {
        var html = renderingService.getHtmlRendering(caller, ProjectIds.parse(projectId), entity(iri, type));
        return new EntityHtmlDto(EntityDataDto.of(html.entity()), html.html());
    }

    @Operation(summary = "The axioms that mention the entity, a page at a time (permission ViewProject)")
    @GetMapping("/entities/usage")
    public UsageDto usage(@Caller UserId caller,
                          @PathVariable String projectId,
                          @RequestParam(required = false) String iri,
                          @RequestParam(required = false) String type,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "50") int size) {
        var usage = entityService.getUsage(caller, ProjectIds.parse(projectId), entity(iri, type),
                                           new UsageFilter(), page(page, size, MAX_PAGE_SIZE));
        return new UsageDto(EntityNodeDto.of(usage.subject()), usage.axiomCount(),
                            PageDto.of(usage.references(), UsageReferenceDto::of));
    }

    @Operation(summary = "The deprecated entities, a page at a time (permission ViewProject)")
    @GetMapping("/entities/deprecated")
    public PageDto<EntityDataDto> deprecated(@Caller UserId caller,
                                             @PathVariable String projectId,
                                             @RequestParam(required = false) String types,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "50") int size) {
        return PageDto.of(entityService.getDeprecatedEntities(caller, ProjectIds.parse(projectId),
                                                              entityTypes(types, ALL_TYPES),
                                                              page(page, size, MAX_PAGE_SIZE)),
                          EntityDataDto::of);
    }

    @Operation(summary = "Creates an entity for each line of sourceText below the parents "
            + "(permissions EditOntology and CreateClass, CreateProperty or CreateIndividual)",
               description = "The parents of individuals are their types. A line that names an existing entity "
                       + "of the type creates nothing.")
    @PostMapping("/entities")
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeOutcomeDto<EntitiesDto> create(@Caller UserId caller,
                                                @PathVariable String projectId,
                                                @RequestBody CreateRequest request) {
        var outcome = entityService.createEntities(caller,
                                                   ProjectIds.parse(projectId),
                                                   OntologyParams.entityType(request.entityType()),
                                                   Objects.requireNonNullElse(request.sourceText(), ""),
                                                   Objects.requireNonNullElse(request.langTag(), ""),
                                                   Set.copyOf(Objects.requireNonNullElse(request.parents(),
                                                                                         List.of())),
                                                   request.commitMessage());
        return ChangeOutcomeDto.of(outcome,
                                   nodes -> new EntitiesDto(nodes.stream().map(EntityNodeDto::of).toList()));
    }

    @Operation(summary = "Deletes the entities and the axioms that mention them "
            + "(permissions EditOntology and DeleteClass, DeleteProperty, DeleteIndividual or DeleteDatatype)")
    @DeleteMapping("/entities")
    public ChangeOutcomeDto<DeletedDto> delete(@Caller UserId caller,
                                               @PathVariable String projectId,
                                               @RequestBody DeleteRequest request) {
        var outcome = entityService.deleteEntities(caller, ProjectIds.parse(projectId),
                                                   new LinkedHashSet<>(Objects.requireNonNullElse(
                                                           request.entities(), List.of())));
        return ChangeOutcomeDto.of(outcome, deleted -> new DeletedDto(List.copyOf(deleted)));
    }

    @Operation(summary = "As DELETE /entities, for clients that cannot send a body with DELETE")
    @PostMapping("/entities/delete")
    public ChangeOutcomeDto<DeletedDto> deleteByPost(@Caller UserId caller,
                                                     @PathVariable String projectId,
                                                     @RequestBody DeleteRequest request) {
        return delete(caller, projectId, request);
    }

    @Operation(summary = "Merges the source entities into the target (permissions EditOntology and "
            + "MergeEntities)",
               description = "treatment DELETE deletes the merged entities, DEPRECATE keeps them deprecated.")
    @PostMapping("/entities/merge")
    public ChangeOutcomeDto<EntityResultDto> merge(@Caller UserId caller,
                                                   @PathVariable String projectId,
                                                   @RequestBody MergeRequest request) {
        var treatment = constant(Treatment.class, Objects.requireNonNullElse(request.treatment(), "DELETE"),
                                 "treatment");
        var outcome = entityService.mergeEntities(caller,
                                                  ProjectIds.parse(projectId),
                                                  new LinkedHashSet<>(Objects.requireNonNullElse(
                                                          request.sourceEntities(), List.of())),
                                                  required(request.targetEntity(), "targetEntity"),
                                                  treatment.treatment,
                                                  request.commitMessage());
        return ChangeOutcomeDto.of(outcome, EntityResultDto::new);
    }

    @Operation(summary = "Gives the entity a new IRI everywhere (permission EditOntology)",
               description = "409 IRI_IN_USE if an entity of the same type has the IRI already.")
    @PostMapping("/entities/change-iri")
    public ChangeOutcomeDto<RenamedDto> changeIri(@Caller UserId caller,
                                                  @PathVariable String projectId,
                                                  @RequestBody ChangeIriRequest request) {
        var outcome = entityService.changeEntityIri(caller,
                                                    ProjectIds.parse(projectId),
                                                    required(request.entity(), "entity"),
                                                    OntologyParams.iri(request.newIri()),
                                                    request.commitMessage());
        return ChangeOutcomeDto.of(outcome, renamed -> new RenamedDto(EntityDataDto.of(renamed.before()),
                                                                      EntityDataDto.of(renamed.after())));
    }

    @Operation(summary = "Makes the classes subclasses of the parent only (permission EditOntology)")
    @PostMapping("/entities/bulk/move-to-parent")
    public ChangeOutcomeDto<ChangedDto> moveToParent(@Caller UserId caller,
                                                     @PathVariable String projectId,
                                                     @RequestBody MoveToParentRequest request) {
        var outcome = bulkEditService.moveToParent(caller,
                                                   ProjectIds.parse(projectId),
                                                   entities(request.entities()),
                                                   required(request.parent(), "parent"),
                                                   request.commitMessage());
        return ChangeOutcomeDto.of(outcome, ChangedDto::new);
    }

    @Operation(summary = "Replaces the entities' values for the property with the value (permission "
            + "EditOntology)",
               description = "Entities without a value for the property get none, as before.")
    @PostMapping("/entities/bulk/set-annotation")
    public ChangeOutcomeDto<ChangedDto> setAnnotation(@Caller UserId caller,
                                                      @PathVariable String projectId,
                                                      @RequestBody SetAnnotationRequest request) {
        var outcome = bulkEditService.setAnnotationValue(caller,
                                                         ProjectIds.parse(projectId),
                                                         entities(request.entities()),
                                                         required(request.property(), "property"),
                                                         required(request.value(), "value"),
                                                         request.commitMessage());
        return ChangeOutcomeDto.of(outcome, entities -> new ChangedDto(!outcome.revision().isEmpty()));
    }

    @Operation(summary = "Replaces, adds to or deletes the entities' literal annotations that match "
            + "(permission EditOntology)",
               description = "operation is REPLACE, AUGMENT or DELETE. The annotations that match have the "
                       + "property (any if missing), a value that is lexicalValueExpression (a regular expression "
                       + "that may match part of the value when regEx is true) and the language langTag.")
    @PostMapping("/entities/bulk/edit-annotations")
    public ChangeOutcomeDto<ChangedDto> editAnnotations(@Caller UserId caller,
                                                        @PathVariable String projectId,
                                                        @RequestBody EditAnnotationsRequest request) {
        var operation = constant(org.industrial.ontology.domain.bulkop.Operation.class, request.operation(),
                                 "operation");
        var newAnnotation = Objects.requireNonNullElse(request.newAnnotation(),
                                                       new NewAnnotationDto(null, null, null));
        return editAnnotations(caller, projectId, request.entities(), operation, request.property(),
                               request.lexicalValueExpression(), request.regEx(), request.langTag(),
                               NewAnnotationData.get(Optional.ofNullable(newAnnotation.property()),
                                                     Optional.ofNullable(newAnnotation.value()),
                                                     Optional.ofNullable(newAnnotation.langTag())),
                               request.commitMessage());
    }

    @Operation(summary = "Deletes the entities' literal annotations that match (permission EditOntology)")
    @PostMapping("/entities/bulk/delete-annotations")
    public ChangeOutcomeDto<ChangedDto> deleteAnnotations(@Caller UserId caller,
                                                          @PathVariable String projectId,
                                                          @RequestBody DeleteAnnotationsRequest request) {
        return editAnnotations(caller, projectId, request.entities(),
                               org.industrial.ontology.domain.bulkop.Operation.DELETE, request.property(),
                               request.lexicalValueExpression(), request.regEx(), request.langTag(),
                               NewAnnotationData.get(Optional.empty(), Optional.empty(), Optional.empty()),
                               request.commitMessage());
    }

    @Operation(summary = "The individuals of the type, or all of them, a page at a time (permission "
            + "ViewProject)",
               description = "mode is ALL (with the individuals of subclasses) or DIRECT; q filters by name.")
    @GetMapping("/individuals")
    public IndividualsDto individuals(@Caller UserId caller,
                                      @PathVariable String projectId,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(defaultValue = "ALL") String mode,
                                      @RequestParam(defaultValue = "") String q,
                                      @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        var cls = type == null ? null : entity(type, EntityType.CLASS).asOWLClass();
        var individuals = individualsService.getIndividuals(caller, ProjectIds.parse(projectId), cls,
                                                            mode(mode), q, page(page, size, MAX_PAGE_SIZE));
        return new IndividualsDto(individuals.type().map(EntityDataDto::of).orElse(null),
                                  individuals.count(),
                                  PageDto.of(individuals.individuals(), EntityNodeDto::of));
    }

    @Operation(summary = "The page of 200 individuals that contains the individual (permission ViewProject)")
    @GetMapping("/individuals/page-containing")
    public PageContainingDto pageContaining(@Caller UserId caller,
                                            @PathVariable String projectId,
                                            @RequestParam(required = false) String iri,
                                            @RequestParam(required = false) String type,
                                            @RequestParam(defaultValue = "DIRECT") String mode) {
        var individual = entity(iri, EntityType.NAMED_INDIVIDUAL).asOWLNamedIndividual();
        var cls = type == null ? null : entity(type, EntityType.CLASS).asOWLClass();
        var page = individualsService.getPageContaining(caller, ProjectIds.parse(projectId), individual, cls,
                                                        mode(mode));
        return new PageContainingDto(EntityNodeDto.of(page.individual()),
                                     EntityNodeDto.of(page.type()),
                                     page.mode() == InstanceRetrievalMode.ALL_INSTANCES ? "ALL" : "DIRECT",
                                     page.types().stream().map(EntityNodeDto::of).toList(),
                                     PageDto.of(page.individuals(), EntityNodeDto::of));
    }

    private ChangeOutcomeDto<ChangedDto> editAnnotations(UserId caller,
                                                         String projectId,
                                                         @Nullable List<OWLEntity> entities,
                                                         org.industrial.ontology.domain.bulkop.Operation operation,
                                                         @Nullable OWLAnnotationProperty property,
                                                         @Nullable String lexicalValueExpression,
                                                         boolean regEx,
                                                         @Nullable String langTag,
                                                         NewAnnotationData newAnnotation,
                                                         @Nullable String commitMessage) {
        var outcome = bulkEditService.editAnnotations(caller,
                                                      ProjectIds.parse(projectId),
                                                      entities(entities),
                                                      operation,
                                                      Optional.ofNullable(property),
                                                      Optional.ofNullable(lexicalValueExpression),
                                                      regEx,
                                                      Optional.ofNullable(langTag),
                                                      newAnnotation,
                                                      commitMessage);
        return ChangeOutcomeDto.of(outcome, changed -> new ChangedDto(outcome.revision().isPresent()));
    }

    private static Set<OWLEntity> entities(@Nullable List<OWLEntity> entities) {
        return new LinkedHashSet<>(Objects.requireNonNullElse(entities, List.of()));
    }

    private static InstanceRetrievalMode mode(String mode) {
        return switch (mode.strip().toUpperCase(Locale.ROOT)) {
            case "ALL", "ALL_INSTANCES" -> InstanceRetrievalMode.ALL_INSTANCES;
            case "DIRECT", "DIRECT_INSTANCES" -> InstanceRetrievalMode.DIRECT_INSTANCES;
            default -> throw WpException.invalidRequest("mode is ALL or DIRECT");
        };
    }

    private static EntitySearchFilterId filterId(String id) {
        try {
            return EntitySearchFilterId.get(id);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw WpException.invalidRequest("A search filter id is a UUID");
        }
    }

    private static int orOne(@Nullable Integer page) {
        return page == null ? 1 : page;
    }

    private static int orSize(@Nullable Integer size, int defaultSize) {
        return size == null ? defaultSize : size;
    }

    private enum Treatment {

        DELETE(MergedEntityTreatment.DELETE_MERGED_ENTITY),

        DEPRECATE(MergedEntityTreatment.DEPRECATE_MERGED_ENTITY);

        private final MergedEntityTreatment treatment;

        Treatment(MergedEntityTreatment treatment) {
            this.treatment = treatment;
        }
    }

    public record LookupResultDto(EntityNodeDto entity, MatchDto match) {
    }

    public record SearchRequest(String searchString,
                                List<String> entityTypes,
                                List<String> langTags,
                                List<String> filters,
                                Integer page,
                                Integer size) {
    }

    public record SearchResultDto(EntityNodeDto entity, List<MatchDto> matches) {
    }

    public record MatchRequest(RootCriteria criteria, Integer page, Integer size) {
    }

    public record EntityHtmlDto(EntityDataDto entity, String html) {
    }

    /**
     * Where an entity is used.
     *
     * @param axiomCount how many axioms mention the entity
     */
    public record UsageDto(EntityNodeDto entity, int axiomCount, PageDto<UsageReferenceDto> references) {
    }

    /**
     * An axiom that mentions the entity, rendered, with the entity that the axiom is about if any.
     */
    public record UsageReferenceDto(String axiomType,
                                    String axiomRendering,
                                    @Nullable OWLEntity subject,
                                    String subjectRendering) {

        static UsageReferenceDto of(UsageReference reference) {
            return new UsageReferenceDto(reference.getAxiomType().getName(),
                                         reference.getAxiomRendering(),
                                         reference.getAxiomSubject().orElse(null),
                                         reference.getSubjectRendering());
        }
    }

    public record CreateRequest(String entityType,
                                String sourceText,
                                String langTag,
                                List<OWLEntity> parents,
                                String commitMessage) {
    }

    public record EntitiesDto(List<EntityNodeDto> entities) {
    }

    public record DeleteRequest(List<OWLEntity> entities) {
    }

    public record DeletedDto(List<OWLEntity> entities) {
    }

    public record MergeRequest(List<OWLEntity> sourceEntities,
                               OWLEntity targetEntity,
                               String treatment,
                               String commitMessage) {
    }

    public record EntityResultDto(OWLEntity entity) {
    }

    public record ChangeIriRequest(OWLEntity entity, String newIri, String commitMessage) {
    }

    public record RenamedDto(EntityDataDto before, EntityDataDto after) {
    }

    public record MoveToParentRequest(List<OWLEntity> entities, OWLEntity parent, String commitMessage) {
    }

    public record SetAnnotationRequest(List<OWLEntity> entities,
                                       OWLAnnotationProperty property,
                                       OWLAnnotationValue value,
                                       String commitMessage) {
    }

    public record NewAnnotationDto(OWLAnnotationProperty property, String value, String langTag) {
    }

    public record EditAnnotationsRequest(List<OWLEntity> entities,
                                         String operation,
                                         OWLAnnotationProperty property,
                                         String lexicalValueExpression,
                                         boolean regEx,
                                         String langTag,
                                         NewAnnotationDto newAnnotation,
                                         String commitMessage) {
    }

    public record DeleteAnnotationsRequest(List<OWLEntity> entities,
                                           OWLAnnotationProperty property,
                                           String lexicalValueExpression,
                                           boolean regEx,
                                           String langTag,
                                           String commitMessage) {
    }

    /**
     * Whether the change changed anything.
     */
    public record ChangedDto(boolean changed) {
    }

    public record IndividualsDto(@Nullable EntityDataDto type, long totalCount, PageDto<EntityNodeDto> individuals) {
    }

    public record PageContainingDto(EntityNodeDto individual,
                                    EntityNodeDto type,
                                    String mode,
                                    List<EntityNodeDto> types,
                                    PageDto<EntityNodeDto> individuals) {
    }
}
