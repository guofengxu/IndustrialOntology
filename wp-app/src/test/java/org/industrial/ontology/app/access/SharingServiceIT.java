package org.industrial.ontology.app.access;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.sharing.PersonId;
import org.industrial.ontology.domain.sharing.ProjectSharingSettings;
import org.industrial.ontology.domain.sharing.SharingPermission;
import org.industrial.ontology.domain.sharing.SharingSetting;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_SHARING_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_MANAGE;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.core.BuiltInRole.SYSTEM_ADMIN;
import static org.industrial.ontology.domain.sharing.SharingPermission.COMMENT;
import static org.industrial.ontology.domain.sharing.SharingPermission.EDIT;
import static org.industrial.ontology.domain.sharing.SharingPermission.MANAGE;
import static org.industrial.ontology.domain.sharing.SharingPermission.VIEW;

/**
 * {@link SharingService} (07 5.1-10), the port of the legacy {@code ProjectSharingSettingsManagerImpl}.
 */
class SharingServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final UserId CAROL = UserId.getUserId("carol");

    private static final UserId DAVE = UserId.getUserId("dave");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    private ProjectTestFixture fixture;

    private SharingService sharingService;

    private AccessManager accessManager;

    private ProjectId projectId;

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
        sharingService = context.bean(SharingService.class);
        accessManager = context.bean(AccessManager.class);
        var users = context.bean(UserRecordRepository.class);
        users.save(UserRecordDocument.of(BOB, "Bob", "bob@example.org", ""));
        users.save(UserRecordDocument.of(CAROL, "Carol", "carol@example.org", ""));
        projectId = fixture.createProject(ALICE, "Pizza");
    }

    private ProjectSharingSettings settings(Optional<SharingPermission> linkSharing, SharingSetting... settings) {
        return new ProjectSharingSettings(projectId, linkSharing, List.of(settings));
    }

    private static SharingSetting share(String person, SharingPermission permission) {
        return new SharingSetting(new PersonId(person), permission);
    }

    @Test
    void aNewProjectShouldBeSharedWithItsOwnerOnly() {
        var settings = sharingService.getSharingSettings(ALICE, projectId);

        assertThat(settings.getSharingSettings()).containsExactly(share("alice", MANAGE));
        assertThat(settings.getLinkSharingPermission()).isEmpty();
    }

    @Test
    void theSettingsShouldReplaceEveryRoleAssignmentOnTheProject() {
        var project = ProjectResource.of(projectId);

        var stored = sharingService.setSharingSettings(ALICE, settings(Optional.of(VIEW),
                                                                       share("carol", COMMENT),
                                                                       share("bob", EDIT),
                                                                       share("alice", MANAGE)));

        assertThat(stored.getSharingSettings()).containsExactly(share("alice", MANAGE),
                                                                share("bob", EDIT),
                                                                share("carol", COMMENT));
        assertThat(stored.getLinkSharingPermission()).contains(VIEW);
        assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(stored);
        assertThat(accessManager.getAssignedRoles(Subject.forUser(ALICE), project))
                .as("the owner's ProjectDownloader role is replaced, as in the legacy manager")
                .containsExactly(CAN_MANAGE.getRoleId());
        assertThat(accessManager.getAssignedRoles(Subject.forAnySignedInUser(), project))
                .containsExactly(CAN_VIEW.getRoleId());
        assertThat(accessManager.hasPermission(Subject.forUser(BOB), project, EDIT_ONTOLOGY)).isTrue();
        assertThat(accessManager.hasPermission(Subject.forUser(DAVE), project, VIEW_PROJECT)).isTrue();

        sharingService.setSharingSettings(ALICE, settings(Optional.empty(), share("alice", MANAGE)));

        assertThat(accessManager.getAssignedRoles(Subject.forUser(BOB), project)).isEmpty();
        assertThat(accessManager.hasPermission(Subject.forUser(BOB), project, VIEW_PROJECT)).isFalse();
        assertThat(accessManager.hasPermission(Subject.forUser(DAVE), project, VIEW_PROJECT)).isFalse();
        assertThat(sharingService.getSharingSettings(ALICE, projectId).getSharingSettings())
                .containsExactly(share("alice", MANAGE));
    }

    @Test
    void unknownUsersShouldBeRefusedWithoutAnyChange() {
        var before = sharingService.getSharingSettings(ALICE, projectId);

        assertThatThrownBy(() -> sharingService.setSharingSettings(ALICE, settings(Optional.of(EDIT),
                                                                                   share("alice", MANAGE),
                                                                                   share("nobody", EDIT),
                                                                                   share(" ", VIEW))))
                .isInstanceOf(WpException.class)
                .hasMessageContaining("nobody")
                .extracting("code", "status").containsExactly(SharingService.USER_NOT_FOUND, 400);
        assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(before);
    }

    @Test
    void usersWithARoleButNoRecordShouldSurviveARoundTrip() {
        sharingService.setUserPermission(projectId, UserId.getUserId("erin"), VIEW);

        var settings = sharingService.getSharingSettings(ALICE, projectId);
        assertThat(settings.getSharingSettings()).contains(share("erin", VIEW));

        assertThat(sharingService.setSharingSettings(ALICE, settings)).isEqualTo(settings);
    }

    @Test
    void theFirstSettingForAUserShouldCount() {
        // So that the e-mail address is looked up
        fixture.grantApplicationRoles(ALICE, SYSTEM_ADMIN);

        var stored = sharingService.setSharingSettings(ALICE, settings(Optional.empty(),
                                                                       share("alice", MANAGE),
                                                                       share("bob", EDIT),
                                                                       share("bob", VIEW),
                                                                       share("bob@example.org", MANAGE)));

        assertThat(stored.getSharingSettings()).containsExactly(share("alice", MANAGE), share("bob", EDIT));
    }

    @Test
    void theOwnerShouldKeepManageWhenACollaboratorLeavesTheOwnerOut() {
        fixture.grantProjectRoles(BOB, projectId, CAN_MANAGE);

        var stored = sharingService.setSharingSettings(BOB, settings(Optional.empty(), share("bob", MANAGE)));

        assertThat(stored.getSharingSettings()).containsExactly(share("alice", MANAGE), share("bob", MANAGE));
        assertThat(accessManager.hasPermission(Subject.forUser(ALICE), ProjectResource.of(projectId),
                                               EDIT_SHARING_SETTINGS)).isTrue();
    }

    @Test
    void nobodyShouldGiveTheOwnerLessThanManage() {
        fixture.grantProjectRoles(BOB, projectId, CAN_MANAGE);
        var before = sharingService.getSharingSettings(ALICE, projectId);

        for (var caller : List.of(BOB, ALICE)) {
            assertThatThrownBy(() -> sharingService.setSharingSettings(caller, settings(Optional.empty(),
                                                                                        share("alice", VIEW),
                                                                                        share("bob", MANAGE))))
                    .isInstanceOf(WpException.class)
                    .extracting("code", "status").containsExactly(SharingService.OWNER_ACCESS_REQUIRED, 400);
        }
        assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(before);
    }

    /**
     * The stored settings show user names, so looking up an e-mail address would tell the caller whose it is.
     */
    @Test
    void onlyUsersWhoMayViewAnyUsersDetailsShouldNameUsersByEmailAddress() {
        var byEmailAddress = settings(Optional.empty(), share("alice", MANAGE), share("carol@example.org", COMMENT));
        var before = sharingService.getSharingSettings(ALICE, projectId);

        assertThatThrownBy(() -> sharingService.setSharingSettings(ALICE, byEmailAddress))
                .isInstanceOf(WpException.class)
                .hasMessage("No user has the name carol@example.org")
                .extracting("code", "status").containsExactly(SharingService.USER_NOT_FOUND, 400);
        assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(before);

        fixture.grantApplicationRoles(ALICE, SYSTEM_ADMIN);
        assertThat(sharingService.setSharingSettings(ALICE, byEmailAddress).getSharingSettings())
                .containsExactly(share("alice", MANAGE), share("carol", COMMENT));
    }

    @Test
    void anEmailAddressThatSeveralUsersHaveShouldBeRefused() {
        context.bean(UserRecordRepository.class).save(UserRecordDocument.of(DAVE, "Dave", "carol@example.org", ""));
        fixture.grantApplicationRoles(ALICE, SYSTEM_ADMIN);
        var before = sharingService.getSharingSettings(ALICE, projectId);

        assertThatThrownBy(() -> sharingService.setSharingSettings(ALICE, settings(Optional.empty(),
                                                                                   share("alice", MANAGE),
                                                                                   share("carol@example.org",
                                                                                         COMMENT))))
                .isInstanceOf(WpException.class)
                .hasMessage("More than one user has the e-mail address carol@example.org")
                .extracting("code", "status").containsExactly(SharingService.USER_NOT_FOUND, 400);
        assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(before);
    }

    @Test
    void onlyUsersWhoMayEditTheSharingSettingsShouldReadOrWriteThem() {
        fixture.grantProjectRoles(BOB, projectId, CAN_EDIT);

        assertThatThrownBy(() -> sharingService.getSharingSettings(BOB, projectId))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("EditSharingSettings");
        assertThatThrownBy(() -> sharingService.setSharingSettings(BOB, settings(Optional.of(MANAGE))))
                .isInstanceOf(PermissionDeniedException.class);
        var unknown = new ProjectSharingSettings(ProjectId.get(UUID.randomUUID().toString()), Optional.empty(),
                                                 List.of());
        assertThatThrownBy(() -> sharingService.setSharingSettings(ALICE, unknown))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void aUserPermissionShouldReplaceTheUsersRolesOnTheProject() {
        var project = ProjectResource.of(projectId);

        sharingService.setUserPermission(projectId, ALICE, VIEW);

        assertThat(accessManager.getAssignedRoles(Subject.forUser(ALICE), project))
                .containsExactly(CAN_VIEW.getRoleId());
        assertThatThrownBy(() -> sharingService.setUserPermission(projectId, UserId.getGuest(), VIEW))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sharingService.setUserPermission(ProjectId.get(UUID.randomUUID().toString()),
                                                                  BOB, VIEW))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    /**
     * The legacy {@code GetProjectSharingSettingsActionHandler_TestCase} and
     * {@code SetProjectSharingSettingsActionHandler_TestCase}.
     */
    @Nested
    class Legacy {

        @Test
        void shouldReturnSharingSettings() {
            assertThat(sharingService.getSharingSettings(ALICE, projectId).getProjectId()).isEqualTo(projectId);
        }

        @Test
        void shouldSetSettings() {
            var settings = settings(Optional.of(COMMENT), share("alice", MANAGE), share("bob", VIEW));

            sharingService.setSharingSettings(ALICE, settings);

            assertThat(sharingService.getSharingSettings(ALICE, projectId)).isEqualTo(settings);
        }
    }
}
