package org.industrial.ontology.cli;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ExternalRoles;
import org.industrial.ontology.app.access.MongoAccessManager;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.RoleOracle;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.industrial.ontology.cli.CliTestSupport.plain;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_MANAGE;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_DOWNLOADER;

/**
 * {@code wp-cli set-permissions} (07 8-4). The command runs in its own Spring context, as it would in its own
 * process, and an access manager of the test's own, as a running server has, sees the new role at once (S6
 * completion criterion), because permissions are not cached.
 */
@ExtendWith(OutputCaptureExtension.class)
class SetPermissionsCommandIT {

    private static final UserId BOB = UserId.getUserId("bob");

    private final String databaseName = MongoTestServer.uniqueDatabase();

    private final ProjectId projectId = ProjectId.get(UUID.randomUUID().toString());

    private MongoClient client;

    private MongoTemplate mongo;

    private AccessManager accessManager;

    @BeforeEach
    void connect() {
        client = MongoClients.create(MongoTestServer.uri(databaseName));
        mongo = new MongoTemplate(client, databaseName);
        accessManager = new MongoAccessManager(RoleOracle.get(), new RoleAssignmentRepository(mongo),
                                               ExternalRoles.NONE);
        var alice = UserId.getUserId("alice");
        new MongoProjectDetailsRepository(mongo, new JacksonDocumentMapper())
                .save(ProjectDetails.get(projectId, "Pizza", "", alice, false, DictionaryLanguage.rdfsLabel("en"),
                                         DisplayNameSettings.empty(), 0L, alice, 0L, alice));
    }

    @AfterEach
    void dropDatabase() {
        mongo.getDb().drop();
        client.close();
    }

    private int run(String... args) {
        return CliTestSupport.run(MongoTestServer.uri(databaseName), args);
    }

    @Test
    void theRoleShouldTakeEffectAtOnce(CapturedOutput output) {
        var bob = Subject.forUser(BOB);
        var project = ProjectResource.of(projectId);
        assertThat(accessManager.hasPermission(bob, project, VIEW_PROJECT)).isFalse();

        assertThat(run("set-permissions", "--project", projectId.getId(), "--user", "bob", "--role", "CAN_EDIT"))
                .isZero();

        assertThat(accessManager.hasPermission(bob, project, EDIT_ONTOLOGY)).isTrue();
        assertThat(accessManager.getAssignedRoles(bob, project)).containsExactly(CAN_EDIT.getRoleId());
        assertThat(output).contains("Note: bob has not signed in yet; the role applies once they do.")
                          .contains("bob now has CAN_EDIT on project " + projectId.getId() + ".");
    }

    @Test
    void theRoleShouldReplaceTheUsersOtherRolesOnTheProject() {
        var bob = Subject.forUser(BOB);
        var project = ProjectResource.of(projectId);
        accessManager.setAssignedRoles(bob, project, List.of(CAN_MANAGE.getRoleId(), PROJECT_DOWNLOADER.getRoleId()));

        assertThat(run("set-permissions", "--project", projectId.getId(), "--user", "bob", "--role", "CAN_VIEW"))
                .isZero();

        assertThat(accessManager.getAssignedRoles(bob, project)).containsExactly(CAN_VIEW.getRoleId());
        assertThat(accessManager.hasPermission(bob, project, VIEW_PROJECT)).isTrue();
        assertThat(accessManager.hasPermission(bob, project, EDIT_ONTOLOGY)).isFalse();
    }

    @Test
    void shouldRejectUnknownProjectsBadIdsUnknownRolesAndTheGuest(CapturedOutput output) {
        var unknownProject = UUID.randomUUID().toString();

        assertThat(run("set-permissions", "--project", unknownProject, "--user", "bob", "--role", "CAN_VIEW"))
                .isEqualTo(2);
        assertThat(run("set-permissions", "--project", "pizza", "--user", "bob", "--role", "CAN_VIEW"))
                .isEqualTo(2);
        assertThat(run("set-permissions", "--project", projectId.getId(), "--user", "bob", "--role", "OWNER"))
                .isEqualTo(2);
        assertThat(run("set-permissions", "--project", projectId.getId(), "--user", "guest", "--role", "CAN_VIEW"))
                .isEqualTo(2);

        assertThat(plain(output.getErr())).contains("Project " + unknownProject + " does not exist")
                                          .contains("--project needs a project id")
                                          .contains("CAN_VIEW, CAN_COMMENT, CAN_EDIT, CAN_MANAGE")
                                          .contains("Roles cannot be given to the guest user");
        assertThat(mongo.getDb().getCollection("RoleAssignments").countDocuments()).isZero();
    }
}
