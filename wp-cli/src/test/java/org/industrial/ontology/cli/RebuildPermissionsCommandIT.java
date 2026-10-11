package org.industrial.ontology.cli;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.industrial.ontology.app.access.ExternalRoles;
import org.industrial.ontology.app.access.MongoAccessManager;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.access.RoleOracle;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.access.persistence.RoleAssignmentDocument;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.domain.core.ProjectId;
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
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;

/**
 * {@code wp-cli rebuild-permissions} (07 8-3): stored closures that no longer match the assigned roles, as after a
 * change of the built-in roles, are recomputed.
 */
@ExtendWith(OutputCaptureExtension.class)
class RebuildPermissionsCommandIT {

    private final String databaseName = MongoTestServer.uniqueDatabase();

    private MongoClient client;

    private MongoTemplate mongo;

    private RoleAssignmentRepository repository;

    @BeforeEach
    void connect() {
        client = MongoClients.create(MongoTestServer.uri(databaseName));
        mongo = new MongoTemplate(client, databaseName);
        repository = new RoleAssignmentRepository(mongo);
    }

    @AfterEach
    void dropDatabase() {
        mongo.getDb().drop();
        client.close();
    }

    @Test
    void shouldRecomputeStaleClosures(CapturedOutput output) {
        var projectId = ProjectId.get(UUID.randomUUID().toString());
        repository.save(new RoleAssignmentDocument(null, "bob", projectId.getId(),
                                                   List.of(CAN_EDIT.getRoleId().getId()), List.of(), List.of()));
        var accessManager = new MongoAccessManager(RoleOracle.get(), repository, ExternalRoles.NONE);
        var bob = Subject.forUser("bob");
        var project = ProjectResource.of(projectId);
        assertThat(accessManager.hasPermission(bob, project, EDIT_ONTOLOGY)).isFalse();

        assertThat(CliTestSupport.run(MongoTestServer.uri(databaseName), "rebuild-permissions")).isZero();

        var oracle = RoleOracle.get();
        assertThat(repository.findAssignments("bob", projectId.getId())).singleElement().satisfies(assignment -> {
            assertThat(assignment.roleClosure()).isEqualTo(oracle.getRoleClosureIds(List.of(CAN_EDIT.getRoleId())));
            assertThat(assignment.actionClosure())
                    .isEqualTo(oracle.getActionClosureIds(List.of(CAN_EDIT.getRoleId())));
        });
        assertThat(accessManager.hasPermission(bob, project, EDIT_ONTOLOGY)).isTrue();
        assertThat(output).contains("Rebuilding permissions...").contains("Finished rebuilding permissions");
    }
}
