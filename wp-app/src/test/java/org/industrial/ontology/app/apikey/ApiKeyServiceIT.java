package org.industrial.ontology.app.apikey;

import org.bson.Document;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ApiKey;
import org.industrial.ontology.domain.core.ApiKeyId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The API key service on the {@code UserApiKeys} collection; the first four tests are the legacy
 * {@code ApiKeyManager_IT}, converted.
 */
class ApiKeyServiceIT {

    private static final UserId USER_ID = UserId.getUserId("JaneDoe");

    private static final UserId OTHER_USER = UserId.getUserId("JohnDoe");

    private static final String PURPOSE = "Test key";

    private static MongoPersistenceTestContext context;

    private ApiKeyService service;

    private GeneratedApiKey generatedKey;

    private Instant timestamp;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithServices();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void generateApiKey() {
        context.clear();
        service = context.bean(ApiKeyService.class);
        timestamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        generatedKey = service.generateApiKeyForUser(USER_ID, PURPOSE);
    }

    @Test
    void shouldGenerateApiKey() {
        assertThat(generatedKey.apiKey().getKey()).isNotBlank();
    }

    @Test
    void shouldRevokeApiKeys() {
        service.revokeApiKey(USER_ID, generatedKey.details().apiKeyId());

        assertThat(service.getApiKeys(USER_ID)).isEmpty();
        assertThat(service.getUserIdForApiKey(generatedKey.apiKey())).isEmpty();
    }

    @Test
    void shouldFindUserByGeneratedKey() {
        assertThat(service.getUserIdForApiKey(generatedKey.apiKey())).contains(USER_ID);
    }

    @Test
    void shouldFindKeyInfo() {
        var keys = service.getApiKeys(USER_ID);

        assertThat(keys).hasSize(1);
        var details = keys.get(0);
        assertThat(details).isEqualTo(generatedKey.details());
        assertThat(details.createdAt()).isBetween(timestamp, Instant.now());
        assertThat(details.purpose()).isEqualTo(PURPOSE);
    }

    @Test
    void shouldStoreOnlyTheHashOfTheKey() {
        var stored = context.database().getCollection(UserApiKeysDocument.COLLECTION)
                            .find(new Document("_id", USER_ID.getUserName())).first();

        var apiKeys = stored.getList("apiKeys", Document.class);
        assertThat(apiKeys).hasSize(1);
        assertThat(apiKeys.get(0).getString("apiKey")).isEqualTo(ApiKeyService.hash(generatedKey.apiKey()))
                                                     .isNotEqualTo(generatedKey.apiKey().getKey());
    }

    @Test
    void anUnknownKeyShouldBelongToNobody() {
        assertThat(service.getUserIdForApiKey(ApiKey.valueOf("not-a-key"))).isEmpty();
    }

    @Test
    void usersShouldSeeAndRevokeOnlyTheirOwnKeys() {
        var othersKey = service.generateApiKey(OTHER_USER, "Other purpose");

        assertThat(service.getApiKeys(USER_ID)).extracting(ApiKeyDetails::apiKeyId)
                                               .containsExactly(generatedKey.details().apiKeyId());
        assertThatThrownBy(() -> service.revokeApiKey(USER_ID, othersKey.details().apiKeyId()))
                .isInstanceOfSatisfying(WpException.class,
                                        e -> assertThat(e.getCode()).isEqualTo("API_KEY_NOT_FOUND"));
        assertThat(service.getUserIdForApiKey(othersKey.apiKey())).contains(OTHER_USER);
    }

    @Test
    void revokingOneKeyShouldKeepTheOthers() {
        var second = service.generateApiKey(USER_ID, "Second key");

        service.revokeApiKey(USER_ID, generatedKey.details().apiKeyId());

        assertThat(service.getApiKeys(USER_ID)).containsExactly(second.details());
        assertThat(service.getUserIdForApiKey(second.apiKey())).contains(USER_ID);
        assertThatThrownBy(() -> service.revokeApiKey(USER_ID, ApiKeyId.valueOf("no-such-key")))
                .isInstanceOf(WpException.class);
    }

    @Test
    void shouldRejectABlankPurposeAndTheGuest() {
        assertThatThrownBy(() -> service.generateApiKey(USER_ID, "  "))
                .isInstanceOfSatisfying(WpException.class, e -> {
                    assertThat(e.getCode()).isEqualTo("INVALID_REQUEST");
                    assertThat(e.getStatus()).isEqualTo(400);
                });
        assertThatThrownBy(() -> service.generateApiKey(UserId.getGuest(), PURPOSE))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> service.generateApiKeyForUser(UserId.getGuest(), PURPOSE))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.generateApiKey(USER_ID, "  trimmed  ").details().purpose()).isEqualTo("trimmed");
    }
}
