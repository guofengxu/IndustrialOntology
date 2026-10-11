package org.industrial.ontology.cli;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.ExternalRoles;
import org.industrial.ontology.app.access.MongoAccessManager;
import org.industrial.ontology.app.access.RoleOracle;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.domain.core.BuiltInAction;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.industrial.ontology.cli.CliTestSupport.plain;

/**
 * {@code wp-cli create-admin} end to end (07 8-1): afterwards the user's application permissions include
 * {@code EditApplicationSettings}.
 */
@ExtendWith(OutputCaptureExtension.class)
class CreateAdminCommandIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private final String databaseName = MongoTestServer.uniqueDatabase();

    private MongoClient client;

    private MongoTemplate mongo;

    @BeforeEach
    void connect() {
        client = MongoClients.create(MongoTestServer.uri(databaseName));
        mongo = new MongoTemplate(client, databaseName);
    }

    @AfterEach
    void dropDatabase() {
        mongo.getDb().drop();
        client.close();
    }

    private int run(String... args) {
        return CliTestSupport.run(MongoTestServer.uri(databaseName), args);
    }

    private Document storedUser() {
        return mongo.getDb().getCollection("Users").find(new Document("_id", "alice")).first();
    }

    @Test
    void shouldMakeTheUserAnAdministrator(CapturedOutput output) {
        assertThat(run("create-admin", "--user", "alice", "--email", "alice@example.org")).isZero();

        assertThat(output).contains("Created user alice, who is now an administrator (SystemAdmin).");
        assertThat(storedUser()).isEqualTo(new Document("_id", "alice").append("realName", "alice")
                                                                        .append("emailAddress",
                                                                                "alice@example.org"));
        var accessManager = new MongoAccessManager(RoleOracle.get(), new RoleAssignmentRepository(mongo),
                                                   ExternalRoles.NONE);
        assertThat(accessManager.getActionClosure(Subject.forUser(ALICE), ApplicationResource.get()))
                .contains(BuiltInAction.EDIT_APPLICATION_SETTINGS.getActionId());
    }

    @Test
    void shouldSetTheLocalPasswordAndUpdateAnExistingUser(CapturedOutput output) {
        assertThat(run("create-admin", "--user", "alice", "--email", "alice@example.org")).isZero();

        assertThat(run("create-admin", "--user", "alice", "--email", "alice@new.example.org",
                       "--password=correct horse battery")).isZero();

        assertThat(output).contains("Updated user alice").contains("The local password is set");
        var stored = storedUser();
        assertThat(stored.getString("emailAddress")).isEqualTo("alice@new.example.org");
        assertThat(new BCryptPasswordEncoder().matches("correct horse battery",
                                                       stored.getString("localPasswordHash"))).isTrue();
        assertThat(mongo.getDb().getCollection("RoleAssignments").countDocuments()).isEqualTo(1);
    }

    @Test
    void shouldRejectUnacceptableArguments(CapturedOutput output) {
        assertThat(run("create-admin", "--user", "alice", "--email", "not an address")).isEqualTo(2);
        assertThat(plain(output.getErr())).contains("'not an address' is not an e-mail address")
                                          .contains("Usage: wp-cli create-admin");

        assertThat(run("create-admin", "--user", "alice", "--email", "alice@example.org", "--password=short"))
                .isEqualTo(2);
        assertThat(run("create-admin", "--email", "alice@example.org")).isEqualTo(2);
        assertThat(plain(output.getErr())).contains("Missing required option: '--user=<userName>'");
        assertThat(storedUser()).isNull();
    }
}
