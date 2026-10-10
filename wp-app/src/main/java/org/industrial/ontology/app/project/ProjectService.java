package org.industrial.ontology.app.project;

import com.google.common.base.Stopwatch;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessRepository;
import org.industrial.ontology.app.user.persistence.UserActivityDocument;
import org.industrial.ontology.app.user.persistence.UserActivityRepository;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DefaultDisplayNameSettingsFactory;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.project.AvailableProject;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.io.upload.RootOntologyDocumentNotFoundException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toMap;
import static org.industrial.ontology.domain.core.BuiltInAction.CREATE_EMPTY_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInAction.DOWNLOAD_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInAction.MOVE_ANY_PROJECT_TO_TRASH;
import static org.industrial.ontology.domain.core.BuiltInAction.UPLOAD_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_MANAGE;
import static org.industrial.ontology.domain.core.BuiltInRole.LAYOUT_EDITOR;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_DOWNLOADER;

/**
 * Projects as a whole (docs/01 §5.1; stage S6): the legacy {@code GetAvailableProjects(WithPermission)},
 * {@code CreateNewProject}, {@code LoadProject}, {@code GetProjectDetails}, {@code MoveProjectsToTrash} and
 * {@code RemoveProjectsFromTrash} handlers, with {@code ProjectDetailsManagerImpl} and
 * {@code ProjectPermissionsManagerImpl}.
 * <p>
 * A project exists when it has {@code ProjectDetails}; {@link ProjectRegistry} would open any id, so every method
 * that takes a project id checks this first and throws {@link ProjectNotFoundException}, then checks the caller's
 * permission.
 */
public class ProjectService {

    /**
     * The uploaded document named by a new project is not in the uploads directory, or has been used already.
     */
    public static final String UPLOAD_NOT_FOUND = "UPLOAD_NOT_FOUND";

    /**
     * The uploaded document is not an ontology that can be parsed.
     */
    public static final String INVALID_UPLOAD = "INVALID_UPLOAD";

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final ProjectAccessRepository projectAccessRepository;

    private final UserActivityRepository userActivityRepository;

    private final ProjectRegistry projectRegistry;

    private final UploadedProjectImporter importer;

    private final Clock clock;

    public ProjectService(@Nonnull AccessManager accessManager,
                          @Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                          @Nonnull ProjectAccessRepository projectAccessRepository,
                          @Nonnull UserActivityRepository userActivityRepository,
                          @Nonnull ProjectRegistry projectRegistry,
                          @Nonnull UploadedProjectImporter importer,
                          @Nonnull Clock clock) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.projectAccessRepository = checkNotNull(projectAccessRepository);
        this.userActivityRepository = checkNotNull(userActivityRepository);
        this.projectRegistry = checkNotNull(projectRegistry);
        this.importer = checkNotNull(importer);
        this.clock = checkNotNull(clock);
    }

    /**
     * The projects that the caller may view or owns (legacy {@code GetAvailableProjects}), in the order of their
     * details (display name first), with whether the caller may download them and move them to the trash, and when
     * the caller last opened them ({@link AvailableProject#UNKNOWN} if never).
     * <p>
     * As in the legacy {@code ProjectPermissionsManagerImpl}, the caller's own projects are always listed, even if
     * the caller has lost the permission to view them; projects shared only by link sharing are not.
     *
     * @param filter the list to return, or {@code null} for every available project, in the trash or not
     */
    @Nonnull
    public List<AvailableProject> getAvailableProjects(@Nonnull UserId caller, @Nullable ProjectFilter filter) {
        accessManager.requireSignedIn(caller);
        Map<ProjectId, Long> lastOpened = userActivityRepository.getUserActivityRecord(caller)
                                                                .map(UserActivityDocument::recentProjects)
                                                                .orElse(List.of())
                                                                .stream()
                                                                .collect(toMap(recent -> ProjectId.get(
                                                                                       recent.projectId()),
                                                                               recent -> recent.timestamp()
                                                                                               .toEpochMilli(),
                                                                               Math::max));
        var user = Subject.forUser(caller);
        return getReadableProjects(caller).stream()
                                          .filter(details -> filter == null || filter.accepts(details, caller))
                                          .map(details -> AvailableProject.get(
                                                  details,
                                                  accessManager.hasPermission(user,
                                                                              ProjectResource.of(
                                                                                      details.getProjectId()),
                                                                              DOWNLOAD_PROJECT),
                                                  canMoveToTrash(caller, details),
                                                  lastOpened.getOrDefault(details.getProjectId(),
                                                                          AvailableProject.UNKNOWN)))
                                          .sorted()
                                          .toList();
    }

    /**
     * The projects on which the caller's own role assignments allow the action (legacy
     * {@code GetAvailableProjectsWithPermission}), for example the projects that forms can be copied from. Projects
     * without details are left out; the legacy handler failed on them.
     */
    @Nonnull
    public List<ProjectDetails> getProjectsWithPermission(@Nonnull UserId caller, @Nonnull ActionId action) {
        accessManager.requireSignedIn(caller);
        return accessManager.getResourcesAccessibleToSubject(Subject.forUser(caller), action)
                            .stream()
                            .flatMap(resource -> resource.getProjectId().stream())
                            .distinct()
                            .flatMap(projectId -> projectDetailsRepository.findOne(projectId).stream())
                            .sorted()
                            .toList();
    }

    /**
     * Creates a project owned by the caller (legacy {@code CreateNewProject}): empty, or from an uploaded document
     * ({@link UploadService}). The project is loaded, as the legacy handler did, then registered with the caller as
     * its manager and downloader and every signed-in user as a layout editor.
     *
     * @param langTag         the language of the default display name (an {@code rdfs:label} in that language)
     * @param sourceDocument  the uploaded document to import, or {@code null} for an empty project
     * @throws PermissionDeniedException if the caller may not create projects, or upload them when there is a
     *                                   document
     * @throws WpException               {@code INVALID_REQUEST} for a blank display name, {@value #UPLOAD_NOT_FOUND}
     *                                   or {@value #INVALID_UPLOAD} (400) for a document that is missing or cannot be
     *                                   parsed
     */
    @Nonnull
    public ProjectDetails createProject(@Nonnull UserId caller,
                                        @Nonnull String displayName,
                                        @Nonnull String description,
                                        @Nonnull String langTag,
                                        @Nullable DocumentId sourceDocument) {
        accessManager.requireSignedIn(caller);
        accessManager.require(caller, ApplicationResource.get(), CREATE_EMPTY_PROJECT);
        if (sourceDocument != null) {
            accessManager.require(caller, ApplicationResource.get(), UPLOAD_PROJECT);
        }
        if (displayName.isBlank()) {
            throw WpException.invalidRequest("A project needs a display name");
        }
        var projectId = ProjectId.get(UUID.randomUUID().toString());
        if (sourceDocument != null) {
            importSources(projectId, sourceDocument, caller);
        }
        var stopwatch = Stopwatch.createStarted();
        projectRegistry.get(projectId);
        logger.info("{} Created and loaded project in {} ms for {}", projectId,
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), caller);
        registerProject(projectId, caller, displayName.strip(), description, langTag);
        applyDefaultPermissions(projectId, caller);
        return requireProject(projectId);
    }

    /**
     * Opens the project for the caller (legacy {@code LoadProject}): records the access, loads the project if it is
     * not loaded, and adds it to the caller's recent projects.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public ProjectDetails openProject(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        var details = requireProject(projectId);
        accessManager.require(caller, ProjectResource.of(projectId), VIEW_PROJECT);
        var stopwatch = Stopwatch.createStarted();
        projectAccessRepository.logProjectAccess(projectId, caller, clock.millis());
        projectRegistry.get(projectId);
        logger.info("{} Opened project in {} ms for {}", projectId, stopwatch.elapsed(TimeUnit.MILLISECONDS), caller);
        if (!caller.isGuest()) {
            userActivityRepository.addRecentProject(caller, projectId, clock.millis());
        }
        return details;
    }

    /**
     * The project's details (legacy {@code GetProjectDetails}, which did not check permissions).
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public ProjectDetails getProjectDetails(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        var details = requireProject(projectId);
        accessManager.require(caller, ProjectResource.of(projectId), VIEW_PROJECT);
        return details;
    }

    /**
     * Moves the project to the trash (legacy {@code MoveProjectsToTrash}, which checked nothing). The project stays
     * as it is and can be restored with {@link #removeFromTrash}.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException unless the caller owns the project or may move any project to the trash
     */
    @Nonnull
    public ProjectDetails moveToTrash(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        return setInTrash(caller, projectId, true);
    }

    /**
     * Restores the project from the trash (legacy {@code RemoveProjectsFromTrash}, which only its owner could do).
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException unless the caller owns the project or may move any project to the trash
     */
    @Nonnull
    public ProjectDetails removeFromTrash(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        return setInTrash(caller, projectId, false);
    }

    private ProjectDetails setInTrash(UserId caller, ProjectId projectId, boolean inTrash) {
        var details = requireProject(projectId);
        if (!canMoveToTrash(caller, details)) {
            throw new PermissionDeniedException("Only the owner of project " + projectId.getId()
                                                        + ", or a user who may move any project to the trash, can"
                                                        + " move it to or from the trash");
        }
        projectDetailsRepository.setInTrash(projectId, inTrash);
        return requireProject(projectId);
    }

    /**
     * The owner, or a user with {@code MoveAnyProjectToTrash}. That action comes with {@code SystemAdmin}, an
     * application role, so it counts on the application; the legacy client asked on the project, where only a
     * project role could grant it, and that still counts too.
     */
    private boolean canMoveToTrash(UserId caller, ProjectDetails details) {
        if (!caller.isGuest() && details.getOwner().equals(caller)) {
            return true;
        }
        var user = Subject.forUser(caller);
        return accessManager.hasPermission(user, ApplicationResource.get(), MOVE_ANY_PROJECT_TO_TRASH)
                || accessManager.hasPermission(user, ProjectResource.of(details.getProjectId()),
                                               MOVE_ANY_PROJECT_TO_TRASH);
    }

    private Set<ProjectDetails> getReadableProjects(UserId caller) {
        var result = new HashSet<ProjectDetails>();
        accessManager.getResourcesAccessibleToSubject(Subject.forUser(caller), VIEW_PROJECT.getActionId())
                     .stream()
                     .flatMap(resource -> resource.getProjectId().stream())
                     .forEach(projectId -> projectDetailsRepository.findOne(projectId).ifPresent(result::add));
        result.addAll(projectDetailsRepository.findByOwner(caller));
        return result;
    }

    private void importSources(ProjectId projectId, DocumentId sourceDocument, UserId owner) {
        if (!importer.exists(sourceDocument)) {
            throw new WpException(UPLOAD_NOT_FOUND, 400, "There is no uploaded document "
                    + sourceDocument.getDocumentId());
        }
        try {
            importer.importProject(projectId, sourceDocument, owner);
        } catch (RootOntologyDocumentNotFoundException e) {
            throw new WpException(INVALID_UPLOAD, 400, e.getMessage());
        } catch (OWLOntologyCreationException | ZipException e) {
            logger.info("{} Could not import uploaded document {}: {}", projectId, sourceDocument.getDocumentId(),
                        e.getMessage());
            throw new WpException(INVALID_UPLOAD, 400, "The uploaded document is not an ontology that can be read");
        } catch (IOException e) {
            throw new UncheckedIOException("Could not import uploaded document " + sourceDocument.getDocumentId(), e);
        }
    }

    /**
     * Ported from {@code ProjectDetailsManagerImpl.registerProject}.
     */
    private void registerProject(ProjectId projectId,
                                 UserId owner,
                                 String displayName,
                                 String description,
                                 String langTag) {
        var now = clock.millis();
        projectDetailsRepository.save(ProjectDetails.get(
                projectId,
                displayName,
                description,
                owner,
                false,
                DictionaryLanguage.rdfsLabel(langTag),
                new DefaultDisplayNameSettingsFactory().getDefaultDisplayNameSettings(langTag),
                now,
                owner,
                now,
                owner));
    }

    /**
     * Ported from {@code CreateNewProjectActionHandler.applyDefaultPermissions}.
     */
    private void applyDefaultPermissions(ProjectId projectId, UserId owner) {
        var project = ProjectResource.of(projectId);
        accessManager.setAssignedRoles(Subject.forUser(owner),
                                       project,
                                       List.of(CAN_MANAGE.getRoleId(), PROJECT_DOWNLOADER.getRoleId()));
        accessManager.setAssignedRoles(Subject.forAnySignedInUser(), project, Set.of(LAYOUT_EDITOR.getRoleId()));
    }

    private ProjectDetails requireProject(ProjectId projectId) {
        return projectDetailsRepository.findOne(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
