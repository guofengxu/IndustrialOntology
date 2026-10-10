package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessRepository;
import org.industrial.ontology.app.user.persistence.UserActivityRepository;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DefaultDisplayNameSettingsFactory;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.project.AvailableProject;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.AxiomType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_FORMS;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_MANAGE;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.core.BuiltInRole.LAYOUT_EDITOR;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_CREATOR;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_DOWNLOADER;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_UPLOADER;
import static org.industrial.ontology.domain.core.BuiltInRole.SYSTEM_ADMIN;

/**
 * {@link ProjectService} (07 5.1-13) over Mongo and a real project registry, wired by the auto-configurations as in
 * wp-server.
 */
class ProjectServiceIT {

    static final UserId ALICE = UserId.getUserId("alice");

    static final UserId BOB = UserId.getUserId("bob");

    static final UserId CAROL = UserId.getUserId("carol");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectTestFixture fixture;

    private ProjectService projectService;

    private AccessManager accessManager;

    private ProjectRegistry projectRegistry;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory);
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        fixture = new ProjectTestFixture(context);
        projectService = context.bean(ProjectService.class);
        accessManager = context.bean(AccessManager.class);
        projectRegistry = context.bean(ProjectRegistry.class);
    }

    private void grantApplicationRoles(UserId user, BuiltInRole... roles) {
        fixture.grantApplicationRoles(user, roles);
    }

    private void grantProjectRoles(UserId user, ProjectId projectId, BuiltInRole... roles) {
        fixture.grantProjectRoles(user, projectId, roles);
    }

    private ProjectId createProject(UserId owner, String displayName) {
        return fixture.createProject(owner, displayName);
    }

    private DocumentId upload(UserId user, Path file) throws IOException {
        return fixture.upload(user, file);
    }

    @Test
    void anEmptyProjectShouldBeLoadedAndRegisteredWithItsCreatorAsManager() {
        grantApplicationRoles(ALICE, PROJECT_CREATOR);

        var details = projectService.createProject(ALICE, " Pizza ", "Pizzas and toppings", "en", null);

        var projectId = details.getProjectId();
        assertThat(details.getDisplayName()).isEqualTo("Pizza");
        assertThat(details.getDescription()).isEqualTo("Pizzas and toppings");
        assertThat(details.getOwner()).isEqualTo(ALICE);
        assertThat(details.getCreatedBy()).isEqualTo(ALICE);
        assertThat(details.isInTrash()).isFalse();
        assertThat(details.getDefaultDictionaryLanguage()).isEqualTo(DictionaryLanguage.rdfsLabel("en"));
        assertThat(details.getDefaultDisplayNameSettings())
                .isEqualTo(new DefaultDisplayNameSettingsFactory().getDefaultDisplayNameSettings("en"));
        assertThat(context.bean(MongoProjectDetailsRepository.class).findOne(projectId)).contains(details);
        assertThat(projectRegistry.getIfLoaded(projectId)).isPresent();
        var project = ProjectResource.of(projectId);
        assertThat(accessManager.getAssignedRoles(Subject.forUser(ALICE), project))
                .containsExactly(CAN_MANAGE.getRoleId(), PROJECT_DOWNLOADER.getRoleId());
        assertThat(accessManager.getAssignedRoles(Subject.forAnySignedInUser(), project))
                .containsExactly(LAYOUT_EDITOR.getRoleId());
    }

    @Test
    void anUploadedDocumentShouldBecomeTheFirstRevision() throws IOException {
        grantApplicationRoles(ALICE, PROJECT_CREATOR, PROJECT_UPLOADER);
        var documentId = upload(ALICE, PizzaOntology.copyTo(sources));

        var projectId = projectService.createProject(ALICE, "Pizza", "", "en", documentId).getProjectId();

        var project = projectRegistry.get(projectId);
        assertThat(project.revisionManager().getRevisions()).singleElement().satisfies(revision -> {
            assertThat(revision.getUserId()).isEqualTo(ALICE);
            assertThat(revision.getHighLevelDescription()).isEqualTo("Initial import");
        });
        assertThat(project.indexes().get(ProjectOntologiesIndex.class).getOntologyIds().toList())
                .singleElement()
                .satisfies(id -> assertThat(id.getOntologyIRI().isPresent()
                                                    && id.getOntologyIRI().get().equals(PizzaOntology.ONTOLOGY_IRI))
                        .as(id.toString())
                        .isTrue());
        var ontologyId = project.indexes().get(ProjectOntologiesIndex.class).getOntologyIds().findFirst().orElseThrow();
        assertThat(project.indexes().get(AxiomsByTypeIndex.class)
                          .getAxiomsByType(AxiomType.SUBCLASS_OF, ontologyId)
                          .count()).isPositive();
        assertThat(context.bean(UploadedProjectImporter.class).exists(documentId))
                .as("the upload is deleted once imported")
                .isFalse();
    }

    @Test
    void creatingProjectsShouldNeedTheApplicationPermissions() throws IOException {
        assertThatThrownBy(() -> projectService.createProject(BOB, "Pizza", "", "en", null))
                .isInstanceOf(PermissionDeniedException.class);

        grantApplicationRoles(BOB, PROJECT_CREATOR, PROJECT_UPLOADER);
        var documentId = upload(BOB, PizzaOntology.copyTo(sources));
        grantApplicationRoles(BOB, PROJECT_CREATOR);
        assertThatThrownBy(() -> projectService.createProject(BOB, "Pizza", "", "en", documentId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("UploadProject");

        assertThatThrownBy(() -> projectService.createProject(UserId.getGuest(), "Pizza", "", "en", null))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> projectService.createProject(BOB, " ", "", "en", null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThat(context.database().getCollection("ProjectDetails").countDocuments()).isZero();
    }

    @Test
    void missingAndUnreadableUploadsShouldBeRefused() throws IOException {
        grantApplicationRoles(ALICE, PROJECT_CREATOR, PROJECT_UPLOADER);
        var notAnOntology = Files.writeString(sources.resolve("notes.txt"), "These are not the axioms you want.");
        var unreadable = upload(ALICE, notAnOntology);
        // An ontology outside the uploads directory: importing it would also delete it, as imports delete uploads
        var outside = Files.copy(PizzaOntology.copyTo(sources), dataDirectory.resolve("outside.owl"),
                                 StandardCopyOption.REPLACE_EXISTING);

        for (var missing : List.of(new DocumentId(UUID.randomUUID().toString()), new DocumentId("../outside.owl"))) {
            assertThatThrownBy(() -> projectService.createProject(ALICE, "Pizza", "", "en", missing))
                    .isInstanceOf(WpException.class)
                    .extracting("code", "status").containsExactly(ProjectService.UPLOAD_NOT_FOUND, 400);
        }
        assertThat(outside).exists();
        assertThatThrownBy(() -> projectService.createProject(ALICE, "Notes", "", "en", unreadable))
                .isInstanceOf(WpException.class)
                .extracting("code", "status").containsExactly(ProjectService.INVALID_UPLOAD, 400);
        // Zip archives that cannot be used are the user's mistake too, not a server error
        var withoutRoot = upload(ALICE, zipOfPizza(sources.resolve("without-root.zip"), "pizza.owl"));
        assertThatThrownBy(() -> projectService.createProject(ALICE, "Without root", "", "en", withoutRoot))
                .isInstanceOf(WpException.class)
                .extracting("code", "status", "message")
                .containsExactly(ProjectService.INVALID_UPLOAD, 400,
                                 "The zip file should contain one ontology document named root-ontology.owl");
        var escaping = upload(ALICE, zipOfPizza(sources.resolve("escaping.zip"), "../root-ontology.owl"));
        assertThatThrownBy(() -> projectService.createProject(ALICE, "Escaping", "", "en", escaping))
                .isInstanceOf(WpException.class)
                .extracting("code", "status").containsExactly(ProjectService.INVALID_UPLOAD, 400);
        assertThat(context.database().getCollection("ProjectDetails").countDocuments()).isZero();
    }

    private Path zipOfPizza(Path archive, String entryName) throws IOException {
        var pizza = sources.resolve("pizza.owl");
        if (Files.notExists(pizza)) {
            PizzaOntology.copyTo(sources);
        }
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry(entryName));
            Files.copy(pizza, zip);
            zip.closeEntry();
        }
        return archive;
    }

    @Test
    void theListsShouldBeThoseOfTheLegacyProjectManager() {
        var owned = createProject(ALICE, "Owned");
        var trashed = createProject(ALICE, "Trashed");
        projectService.moveToTrash(ALICE, trashed);
        var shared = createProject(BOB, "Shared");
        grantProjectRoles(ALICE, shared, CAN_VIEW);
        var sharedThenTrashed = createProject(BOB, "Shared, then trashed");
        grantProjectRoles(ALICE, sharedThenTrashed, CAN_VIEW);
        projectService.moveToTrash(BOB, sharedThenTrashed);
        createProject(CAROL, "Not shared");
        projectService.openProject(ALICE, shared);

        assertThat(ids(projectService.getAvailableProjects(ALICE, ProjectFilter.OWNED))).containsExactly(owned);
        assertThat(ids(projectService.getAvailableProjects(ALICE, ProjectFilter.SHARED))).containsExactly(shared);
        assertThat(ids(projectService.getAvailableProjects(ALICE, ProjectFilter.TRASH))).containsExactly(trashed);
        var all = projectService.getAvailableProjects(ALICE, null);
        assertThat(ids(all)).containsExactly(owned, shared, sharedThenTrashed, trashed);
        assertThat(all).allSatisfy(project -> assertThat(project.isDownloadable()).isTrue());
        assertThat(all).filteredOn(project -> project.getProjectId().equals(shared))
                       .singleElement()
                       .satisfies(project -> {
                           assertThat(project.isTrashable()).isFalse();
                           assertThat(project.getLastOpenedAt()).isPositive();
                       });
        assertThat(all).filteredOn(project -> project.getProjectId().equals(owned))
                       .singleElement()
                       .satisfies(project -> {
                           assertThat(project.isTrashable()).isTrue();
                           assertThat(project.getLastOpenedAt()).isEqualTo(AvailableProject.UNKNOWN);
                       });
    }

    @Test
    void ownedProjectsShouldBeListedEvenWithoutRoles() {
        var projectId = createProject(ALICE, "Lost my roles");
        grantProjectRoles(ALICE, projectId);

        assertThat(ids(projectService.getAvailableProjects(ALICE, ProjectFilter.OWNED))).containsExactly(projectId);
    }

    @Test
    void openingAProjectShouldLoadItAndRecordTheAccess() {
        var projectId = createProject(ALICE, "Pizza");
        projectRegistry.close(projectId);

        var details = projectService.openProject(ALICE, projectId);

        assertThat(details.getProjectId()).isEqualTo(projectId);
        assertThat(projectRegistry.getIfLoaded(projectId)).isPresent();
        assertThat(context.bean(ProjectAccessRepository.class).findAccess(projectId, ALICE))
                .hasValueSatisfying(access -> assertThat(access.count()).isEqualTo(1));
        assertThat(context.bean(UserActivityRepository.class).getUserActivityRecord(ALICE))
                .hasValueSatisfying(activity -> assertThat(activity.recentProjects())
                        .singleElement()
                        .satisfies(recent -> assertThat(recent.projectId()).isEqualTo(projectId.getId())));
        assertThatThrownBy(() -> projectService.openProject(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> projectService.getProjectDetails(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void unknownProjectsShouldBeNotFoundForEveryone() {
        var unknown = ProjectId.get(UUID.randomUUID().toString());
        grantApplicationRoles(ALICE, SYSTEM_ADMIN);

        assertThatThrownBy(() -> projectService.openProject(ALICE, unknown)).isInstanceOf(ProjectNotFoundException.class);
        assertThatThrownBy(() -> projectService.getProjectDetails(BOB, unknown))
                .isInstanceOf(ProjectNotFoundException.class)
                .extracting("code", "status").containsExactly("PROJECT_NOT_FOUND", 404);
        assertThatThrownBy(() -> projectService.moveToTrash(ALICE, unknown))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThat(projectRegistry.getIfLoaded(unknown)).isEmpty();
    }

    @Test
    void onlyTheOwnerAndUsersWhoMayMoveAnyProjectShouldUseTheTrash() {
        var projectId = createProject(ALICE, "Pizza");
        grantProjectRoles(BOB, projectId, CAN_MANAGE);

        assertThatThrownBy(() -> projectService.moveToTrash(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);

        assertThat(projectService.moveToTrash(ALICE, projectId).isInTrash()).isTrue();
        assertThatThrownBy(() -> projectService.removeFromTrash(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class);
        assertThat(projectService.removeFromTrash(ALICE, projectId).isInTrash()).isFalse();

        grantApplicationRoles(CAROL, SYSTEM_ADMIN);
        assertThat(projectService.moveToTrash(CAROL, projectId).isInTrash()).isTrue();
        assertThat(projectService.removeFromTrash(CAROL, projectId).isInTrash()).isFalse();
    }

    @Test
    void projectsWithAPermissionShouldComeFromTheCallersOwnRoles() {
        var managed = createProject(ALICE, "Managed");
        var viewed = createProject(BOB, "Viewed");
        grantProjectRoles(ALICE, viewed, CAN_VIEW);

        assertThat(projectService.getProjectsWithPermission(ALICE, EDIT_FORMS.getActionId()))
                .extracting(ProjectDetails::getProjectId)
                .containsExactly(managed);
    }

    private static List<ProjectId> ids(List<AvailableProject> projects) {
        return projects.stream().map(AvailableProject::getProjectId).toList();
    }

    /**
     * The legacy {@code CreateNewProjectActionHandler_TestCase} and {@code GetProjectDetailsActionHandler_TestCase},
     * which mocked the managers; here they run against the real ones.
     */
    @Nested
    class Legacy {

        @Test
        void shouldCreateNewProject() {
            var projectId = createProject(ALICE, "The display name");

            assertThat(context.bean(MongoProjectDetailsRepository.class).containsProject(projectId)).isTrue();
        }

        @Test
        void shouldDenyCreateNewProject() {
            assertThatThrownBy(() -> projectService.createProject(BOB, "The display name", "", "en-GB", null))
                    .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        void shouldRegisterNewProject() {
            grantApplicationRoles(ALICE, PROJECT_CREATOR);

            var details = projectService.createProject(ALICE, "The display name", "The Project Description",
                                                       "en-GB", null);

            assertThat(details.getDisplayName()).isEqualTo("The display name");
            assertThat(details.getDescription()).isEqualTo("The Project Description");
            assertThat(details.getOwner()).isEqualTo(ALICE);
        }

        @Test
        void shouldNotAllowGuestsToCreateProjects() {
            grantApplicationRoles(UserId.getGuest(), PROJECT_CREATOR);

            assertThatThrownBy(() -> projectService.createProject(UserId.getGuest(), "The display name", "", "en",
                                                                  null))
                    .isInstanceOf(PermissionDeniedException.class);
        }

        @Test
        void should_execute() {
            var projectId = createProject(ALICE, "Pizza");

            assertThat(projectService.getProjectDetails(ALICE, projectId).getProjectId()).isEqualTo(projectId);
        }
    }
}
