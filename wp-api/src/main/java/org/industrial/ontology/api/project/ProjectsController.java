package org.industrial.ontology.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.access.PermissionService;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectFilter;
import org.industrial.ontology.app.project.ProjectService;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.project.AvailableProject;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.upload.DocumentId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Projects (docs/02 §3): the project list, creating projects, opening them, the trash and the caller's permissions.
 * The services check the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects", description = "Projects, the trash and the caller's permissions on a project")
public class ProjectsController {

    private final ProjectService projectService;

    private final PermissionService permissionService;

    public ProjectsController(ProjectService projectService, PermissionService permissionService) {
        this.projectService = checkNotNull(projectService);
        this.permissionService = checkNotNull(permissionService);
    }

    @Operation(summary = "The projects that the caller owns or may view",
               description = "filter: owned (the caller's, not in the trash), shared (others', not in the trash) or "
                       + "trash (the caller's, in the trash); without it every available project is listed.")
    @GetMapping
    public List<AvailableProjectDto> projects(@Caller UserId caller, @RequestParam(required = false) String filter) {
        return projectService.getAvailableProjects(caller, parseFilter(filter))
                             .stream()
                             .map(AvailableProjectDto::of)
                             .toList();
    }

    @Operation(summary = "Creates a project owned by the caller (permission CreateEmptyProject)",
               description = "Empty, or from a document uploaded with POST /api/v1/uploads (permission UploadProject "
                       + "too). The caller becomes the project's manager.")
    @PostMapping
    public ResponseEntity<ProjectDto> createProject(@Caller UserId caller, @RequestBody NewProjectRequest request) {
        var sourceDocument = request.sourceDocumentId() == null || request.sourceDocumentId().isBlank()
                ? null
                : new DocumentId(request.sourceDocumentId());
        var details = projectService.createProject(caller,
                                                   Objects.requireNonNullElse(request.displayName(), ""),
                                                   Objects.requireNonNullElse(request.description(), ""),
                                                   Objects.requireNonNullElse(request.language(), ""),
                                                   sourceDocument);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                                                  .path("/{projectId}")
                                                  .buildAndExpand(details.getProjectId().getId())
                                                  .toUri();
        return ResponseEntity.created(location).body(ProjectDto.of(details));
    }

    @Operation(summary = "The project's details (permission ViewProject)")
    @GetMapping("/{projectId}")
    public ProjectDto project(@Caller UserId caller, @PathVariable String projectId) {
        return ProjectDto.of(projectService.getProjectDetails(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Opens the project (permission ViewProject)",
               description = "The legacy LoadProject: loads the project if it is not loaded, records the access and "
                       + "adds the project to the caller's recent projects.")
    @PostMapping("/{projectId}/open")
    public ProjectDto openProject(@Caller UserId caller, @PathVariable String projectId) {
        return ProjectDto.of(projectService.openProject(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Moves the project to the trash (its owner, or permission MoveAnyProjectToTrash)")
    @PostMapping("/{projectId}/trash")
    public ProjectDto moveToTrash(@Caller UserId caller, @PathVariable String projectId) {
        return ProjectDto.of(projectService.moveToTrash(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "Restores the project from the trash (its owner, or permission MoveAnyProjectToTrash)")
    @DeleteMapping("/{projectId}/trash")
    public ProjectDto removeFromTrash(@Caller UserId caller, @PathVariable String projectId) {
        return ProjectDto.of(projectService.removeFromTrash(caller, ProjectIds.parse(projectId)));
    }

    @Operation(summary = "The actions that the caller may perform on the project, as sorted action ids")
    @GetMapping("/{projectId}/permissions")
    public List<String> permissions(@Caller UserId caller, @PathVariable String projectId) {
        return permissionService.getProjectPermissions(caller, ProjectIds.parse(projectId))
                                .stream()
                                .map(ActionId::getId)
                                .sorted()
                                .toList();
    }

    @Nullable
    private static ProjectFilter parseFilter(@Nullable String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }
        try {
            return ProjectFilter.valueOf(filter.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw WpException.invalidRequest("filter is one of owned, shared and trash");
        }
    }

    /**
     * {@code POST /api/v1/projects}; {@code language} is the language tag of the default display name.
     *
     * @param sourceDocumentId the id from {@code POST /api/v1/uploads}, or none for an empty project
     */
    public record NewProjectRequest(String displayName,
                                    String description,
                                    String language,
                                    @Nullable String sourceDocumentId) {
    }

    /**
     * A project's details (the legacy {@code ProjectDetails}).
     */
    public record ProjectDto(String projectId,
                             String displayName,
                             String description,
                             String owner,
                             boolean inTrash,
                             DictionaryLanguage defaultLanguage,
                             DisplayNameSettings defaultDisplayNameSettings,
                             Instant createdAt,
                             String createdBy,
                             Instant modifiedAt,
                             String modifiedBy) {

        static ProjectDto of(ProjectDetails details) {
            return new ProjectDto(details.getProjectId().getId(),
                                  details.getDisplayName(),
                                  details.getDescription(),
                                  details.getOwner().getUserName(),
                                  details.isInTrash(),
                                  details.getDefaultDictionaryLanguage(),
                                  details.getDefaultDisplayNameSettings(),
                                  Instant.ofEpochMilli(details.getCreatedAt()),
                                  details.getCreatedBy().getUserName(),
                                  Instant.ofEpochMilli(details.getLastModifiedAt()),
                                  details.getLastModifiedBy().getUserName());
        }
    }

    /**
     * An entry of the project list (the legacy {@code AvailableProject}): the details, whether the caller may
     * download the project and move it to the trash, and when the caller last opened it ({@code null} if never).
     */
    public record AvailableProjectDto(String projectId,
                                      String displayName,
                                      String description,
                                      String owner,
                                      boolean inTrash,
                                      Instant createdAt,
                                      String createdBy,
                                      Instant modifiedAt,
                                      String modifiedBy,
                                      boolean downloadable,
                                      boolean trashable,
                                      @Nullable Instant lastOpenedAt) {

        static AvailableProjectDto of(AvailableProject project) {
            var details = project.getProjectDetails();
            return new AvailableProjectDto(details.getProjectId().getId(),
                                           details.getDisplayName(),
                                           details.getDescription(),
                                           details.getOwner().getUserName(),
                                           details.isInTrash(),
                                           Instant.ofEpochMilli(details.getCreatedAt()),
                                           details.getCreatedBy().getUserName(),
                                           Instant.ofEpochMilli(details.getLastModifiedAt()),
                                           details.getLastModifiedBy().getUserName(),
                                           project.isDownloadable(),
                                           project.isTrashable(),
                                           project.getLastOpenedAt() == AvailableProject.UNKNOWN
                                                   ? null
                                                   : Instant.ofEpochMilli(project.getLastOpenedAt()));
        }
    }
}
