package org.industrial.ontology.api.ontology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.ontology.OntologyJson.ChangeOutcomeDto;
import org.industrial.ontology.api.ontology.OntologyJson.EntityDataDto;
import org.industrial.ontology.api.ontology.OntologyJson.RenderedFrameDto;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.EntityFrameView;
import org.industrial.ontology.app.frame.FrameService;
import org.industrial.ontology.app.frame.ManchesterSyntaxService;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.FreshEntity;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.ManchesterSyntaxFrameParseError;
import org.industrial.ontology.domain.frame.PlainEntityFrame;
import org.semanticweb.owlapi.model.EntityType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.api.ontology.OntologyParams.entity;
import static org.industrial.ontology.api.ontology.OntologyParams.required;

/**
 * Entity frames (docs/02 §4.3): plain for editing and rendered for showing, and Manchester syntax with its check and
 * completions. {@code frameType} is {@code class}, {@code object-property}, {@code data-property},
 * {@code annotation-property} or {@code individual}. The services check the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/frames")
@Tag(name = "Frames", description = "Entity frames and Manchester syntax")
public class FramesController {

    private static final Map<String, EntityType<?>> FRAME_TYPES = Map.of(
            "class", EntityType.CLASS,
            "object-property", EntityType.OBJECT_PROPERTY,
            "data-property", EntityType.DATA_PROPERTY,
            "annotation-property", EntityType.ANNOTATION_PROPERTY,
            "individual", EntityType.NAMED_INDIVIDUAL);

    private final FrameService frameService;

    private final ManchesterSyntaxService manchesterSyntaxService;

    public FramesController(FrameService frameService, ManchesterSyntaxService manchesterSyntaxService) {
        this.frameService = checkNotNull(frameService);
        this.manchesterSyntaxService = checkNotNull(manchesterSyntaxService);
    }

    @Operation(summary = "The entity's frame, plain and rendered (permission ViewProject)")
    @GetMapping("/{frameType}")
    public FrameDto frame(@Caller UserId caller,
                          @PathVariable String projectId,
                          @PathVariable String frameType,
                          @RequestParam(required = false) String iri) {
        return FrameDto.of(frameService.getFrame(caller, ProjectIds.parse(projectId),
                                                 entity(iri, frameType(frameType))));
    }

    @Operation(summary = "Replaces the frame from with to (permission EditOntology)",
               description = "409 CONFLICT_FRAME_CHANGED when from is no longer the entity's frame; the result "
                       + "is the frame after the change.")
    @PutMapping("/{frameType}")
    public ChangeOutcomeDto<FrameDto> updateFrame(@Caller UserId caller,
                                                  @PathVariable String projectId,
                                                  @PathVariable String frameType,
                                                  @RequestParam(required = false) String iri,
                                                  @RequestBody FrameUpdateRequest request) {
        var project = ProjectIds.parse(projectId);
        var subject = entity(iri, frameType(frameType));
        var from = required(request.from(), "from");
        var to = required(request.to(), "to");
        if (!from.getSubject().equals(subject) || !to.getSubject().equals(subject)) {
            throw WpException.invalidRequest("The frames must be frames of " + subject.getIRI());
        }
        var outcome = frameService.updateFrame(caller, project, from, to, request.commitMessage());
        return ChangeOutcomeDto.of(outcome, entity -> FrameDto.of(frameService.getFrame(caller, project, entity)));
    }

    @Operation(summary = "The entity's frame in Manchester syntax (permission ViewProject)")
    @GetMapping("/manchester")
    public ManchesterFrameDto manchesterFrame(@Caller UserId caller,
                                              @PathVariable String projectId,
                                              @RequestParam(required = false) String iri,
                                              @RequestParam(required = false) String type) {
        var frame = manchesterSyntaxService.getFrame(caller, ProjectIds.parse(projectId), entity(iri, type));
        return new ManchesterFrameDto(EntityDataDto.of(frame.subject()), frame.text());
    }

    @Operation(summary = "Whether the edit of the frame text parses and changes anything (permission ViewProject)",
               description = "status is CHANGED, UNCHANGED or ERROR; an error has its line and column, from 1.")
    @PostMapping("/manchester/check")
    public CheckDto check(@Caller UserId caller,
                          @PathVariable String projectId,
                          @RequestBody CheckRequest request) {
        var check = manchesterSyntaxService.check(caller,
                                                  ProjectIds.parse(projectId),
                                                  entity(request.iri(), request.type()),
                                                  Objects.requireNonNullElse(request.from(), ""),
                                                  Objects.requireNonNullElse(request.to(), ""),
                                                  freshEntities(request.freshEntities()));
        return new CheckDto(check.status().name(), check.status() != ManchesterSyntaxService.CheckStatus.ERROR,
                            check.error() == null ? null : ErrorDto.of(check.error()));
    }

    @Operation(summary = "Replaces the frame text from with to (permission EditOntology)",
               description = "400 MANCHESTER_SYNTAX_ERROR with line and column when a text does not parse; the "
                       + "result is the frame text after the change.")
    @PutMapping("/manchester")
    public ChangeOutcomeDto<ManchesterTextDto> setManchesterFrame(@Caller UserId caller,
                                                                  @PathVariable String projectId,
                                                                  @RequestParam(required = false) String iri,
                                                                  @RequestParam(required = false) String type,
                                                                  @RequestBody ManchesterUpdateRequest request) {
        var outcome = manchesterSyntaxService.setFrame(caller,
                                                       ProjectIds.parse(projectId),
                                                       entity(iri, type),
                                                       required(request.from(), "from"),
                                                       required(request.to(), "to"),
                                                       request.commitMessage(),
                                                       freshEntities(request.freshEntities()));
        return ChangeOutcomeDto.of(outcome, ManchesterTextDto::new);
    }

    @Operation(summary = "What may be typed at the cursor of the frame text (permission ViewProject)",
               description = "cursor is the offset in text; choices replace the text between from and to.")
    @PostMapping("/manchester/completions")
    public ManchesterSyntaxService.Completions completions(@Caller UserId caller,
                                                           @PathVariable String projectId,
                                                           @RequestBody CompletionsRequest request) {
        return manchesterSyntaxService.getCompletions(caller,
                                                      ProjectIds.parse(projectId),
                                                      Objects.requireNonNullElse(request.text(), ""),
                                                      required(request.cursor(), "cursor"),
                                                      freshEntities(request.freshEntities()),
                                                      Objects.requireNonNullElse(request.limit(), 20));
    }

    private static EntityType<?> frameType(String frameType) {
        var type = FRAME_TYPES.get(frameType);
        if (type == null) {
            throw WpException.invalidRequest("frameType is class, object-property, data-property, "
                                                     + "annotation-property or individual");
        }
        return type;
    }

    private static Set<FreshEntity> freshEntities(@Nullable List<FreshEntityDto> freshEntities) {
        return Objects.requireNonNullElse(freshEntities, List.<FreshEntityDto>of())
                      .stream()
                      .map(fresh -> new FreshEntity(required(fresh.name(), "name"),
                                                    OntologyParams.entityType(fresh.type())))
                      .collect(Collectors.toSet());
    }

    /**
     * An entity's frame: plain, which the client edits and sends back as {@code from}, and rendered.
     */
    public record FrameDto(PlainEntityFrame frame, RenderedFrameDto rendered) {

        static FrameDto of(EntityFrameView view) {
            return new FrameDto(view.frame(), RenderedFrameDto.of(view.rendered()));
        }
    }

    public record FrameUpdateRequest(PlainEntityFrame from, PlainEntityFrame to, String commitMessage) {
    }

    public record ManchesterFrameDto(EntityDataDto entity, String frameText) {
    }

    /**
     * A name in the frame text for an entity of the type that does not exist yet; setting the frame creates it.
     */
    public record FreshEntityDto(String name, String type) {
    }

    public record CheckRequest(String iri, String type, String from, String to, List<FreshEntityDto> freshEntities) {
    }

    /**
     * The result of a check; {@code ok} is false when the text does not parse.
     */
    public record CheckDto(String status, boolean ok, @Nullable ErrorDto error) {
    }

    /**
     * Where a frame text failed to parse; lines and columns start at 1.
     */
    public record ErrorDto(String message, int line, int column, String token, List<String> expectedEntityTypes) {

        static ErrorDto of(ManchesterSyntaxFrameParseError error) {
            return new ErrorDto(error.getMessage(), error.getLine(), error.getCol(),
                                Objects.requireNonNullElse(error.getToken(), ""),
                                error.getExpectedEntityTypes().stream().map(EntityType::getName).toList());
        }
    }

    public record ManchesterUpdateRequest(String from,
                                          String to,
                                          String commitMessage,
                                          List<FreshEntityDto> freshEntities) {
    }

    public record ManchesterTextDto(String frameText) {
    }

    public record CompletionsRequest(String text, Integer cursor, Integer limit, List<FreshEntityDto> freshEntities) {
    }
}
