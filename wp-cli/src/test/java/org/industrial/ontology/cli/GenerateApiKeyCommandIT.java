package org.industrial.ontology.cli;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.industrial.ontology.app.access.ExternalRoles;
import org.industrial.ontology.app.access.MongoAccessManager;
import org.industrial.ontology.app.access.RoleOracle;
import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.apikey.persistence.UserApiKeyRepository;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.domain.core.ApiKey;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Clock;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.industrial.ontology.cli.CliTestSupport.plain;

/**
 * {@code wp-cli generate-api-key} end to end (07 8-2): the printed key belongs to the user. That such a key
 * authenticates an HTTP request is wp-api's {@code AuthenticationIT.anApiKeyShouldAuthenticateItsUser}, with a key
 * from the same {@code ApiKeyService.generateApiKeyForUser}.
 */
@ExtendWith(OutputCaptureExtension.class)
class GenerateApiKeyCommandIT {

    private static final Pattern PRINTED_KEY = Pattern.compile("^ {4}(\\S+)$", Pattern.MULTILINE);

    private final String databaseName = MongoTestServer.uniqueDatabase();

    private MongoClient client;

    private MongoTemplate mongo;

    private ApiKeyService apiKeyService;

    @BeforeEach
    void connect() {
        client = MongoClients.create(MongoTestServer.uri(databaseName));
        mongo = new MongoTemplate(client, databaseName);
        var accessManager = new MongoAccessManager(RoleOracle.get(), new RoleAssignmentRepository(mongo),
                                                   ExternalRoles.NONE);
        apiKeyService = new ApiKeyService(new UserApiKeyRepository(mongo), accessManager, Clock.systemUTC());
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
    void thePrintedKeyShouldBelongToTheUser(CapturedOutput output) {
        assertThat(run("generate-api-key", "--user", "integration", "--purpose", "nightly import")).isZero();

        var matcher = PRINTED_KEY.matcher(output.getOut());
        assertThat(matcher.find()).as(output.getOut()).isTrue();
        var key = ApiKey.valueOf(matcher.group(1));
        assertThat(apiKeyService.getUserIdForApiKey(key)).contains(UserId.getUserId("integration"));
        assertThat(apiKeyService.getApiKeys(UserId.getUserId("integration")))
                .singleElement()
                .satisfies(details -> assertThat(details.purpose()).isEqualTo("nightly import"));
        assertThat(output).contains("Note: integration has not signed in yet; the key works anyway.")
                          .contains("This API key cannot be recovered.");
        assertThat(mongo.getDb().getCollection("UserApiKeys").find().first().toJson()).doesNotContain(key.getKey());
    }

    @Test
    void shouldRejectTheGuestAndABlankPurpose(CapturedOutput output) {
        assertThat(run("generate-api-key", "--user", "guest", "--purpose", "anything")).isEqualTo(2);
        assertThat(run("generate-api-key", "--user", "integration", "--purpose", " ")).isEqualTo(2);
        assertThat(plain(output.getErr())).contains("Cannot generate an API key for 'guest'")
                                          .contains("An API key needs a purpose");
        assertThat(mongo.getDb().getCollection("UserApiKeys").countDocuments()).isZero();
    }
}
