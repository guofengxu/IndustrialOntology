package org.industrial.ontology.app.access;

import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_SHARING_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.core.BuiltInRole.SYSTEM_ADMIN;

/**
 * {@link PermissionService} (07 5.1-10): the caller's actions on a project and rebuilding the stored closures.
 */
class PermissionServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    private ProjectTestFixture fixture;

    private PermissionService permissionService;

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
        permissionService = context.bean(PermissionService.class);
    }

    @Test
    void theCallerShouldGetItsOwnActionsOnTheProject() {
        var projectId = fixture.createProject(ALICE, "Pizza");

        assertThat(permissionService.getProjectPermissions(ALICE, projectId))
                .contains(VIEW_PROJECT.getActionId(), EDIT_SHARING_SETTINGS.getActionId());
        assertThat(permissionService.getProjectPermissions(BOB, projectId))
                .as("every signed-in user is a layout editor of a new project")
                .doesNotContain(VIEW_PROJECT.getActionId());

        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
        assertThat(permissionService.getProjectPermissions(BOB, projectId))
                .contains(VIEW_PROJECT.getActionId())
                .doesNotContain(EDIT_ONTOLOGY.getActionId());
    }

    @Test
    void unknownProjectsAndTheGuestShouldBeRefused() {
        var projectId = fixture.createProject(ALICE, "Pizza");

        assertThatThrownBy(() -> permissionService.getProjectPermissions(ALICE,
                                                                         ProjectId.get(UUID.randomUUID().toString())))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThatThrownBy(() -> permissionService.getProjectPermissions(UserId.getGuest(), projectId))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void rebuildingShouldNeedRebuildPermissionsAndRecomputeTheClosures() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        var repository = context.bean(RoleAssignmentRepository.class);
        repository.setAssignedRoles(BOB.getUserName(), projectId.getId(), List.of(CAN_EDIT.getRoleId().getId()),
                                    List.of(), List.of());

        assertThatThrownBy(() -> permissionService.rebuildPermissions(BOB))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("RebuildPermissions");
        assertThat(permissionService.getProjectPermissions(BOB, projectId)).doesNotContain(EDIT_ONTOLOGY.getActionId());

        fixture.grantApplicationRoles(ALICE, SYSTEM_ADMIN);
        permissionService.rebuildPermissions(ALICE);

        assertThat(permissionService.getProjectPermissions(BOB, projectId)).contains(EDIT_ONTOLOGY.getActionId());
        assertThat(repository.findAssignments(BOB.getUserName(), projectId.getId()))
                .singleElement()
                .satisfies(assignment -> assertThat(assignment.actionClosure())
                        .isEqualTo(RoleOracle.get().getActionClosureIds(List.of(CAN_EDIT.getRoleId()))));
    }

    /**
     * The legacy {@code GetProjectPermissionsActionHandler_TestCase}: any user may ask (its validator accepted every
     * request).
     */
    @Nested
    class Legacy {

        @Test
        void shouldAllowAnyOneToRetrievePermissions() {
            var projectId = fixture.createProject(ALICE, "Pizza");

            assertThat(permissionService.getProjectPermissions(UserId.getUserId("anyone"), projectId))
                    .isNotNull()
                    .allSatisfy(action -> assertThat(action).isInstanceOf(ActionId.class));
        }
    }
}
