package org.industrial.ontology.app.apikey.persistence;

import org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument.ApiKeyRecord;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ApiKeyId;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserApiKeyRepositoryIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final ApiKeyId K1 = ApiKeyId.valueOf("aaaaaaaa-0000-4000-8000-000000000001");

    private static final ApiKeyId K2 = ApiKeyId.valueOf("aaaaaaaa-0000-4000-8000-000000000002");

    private static MongoPersistenceTestContext context;

    private UserApiKeyRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        repository = context.bean(UserApiKeyRepository.class);
    }

    @Test
    void shouldAddKeysAndFindTheirUser() {
        repository.addApiKey(ALICE, key(K1, "hash-1", "CI"));
        repository.addApiKey(ALICE, key(K2, "hash-2", "Notebook"));

        assertThat(repository.getApiKeys(ALICE)).extracting(ApiKeyRecord::purpose).containsExactly("CI", "Notebook");
        assertThat(repository.getUserIdForApiKey("hash-2")).contains(ALICE);
        assertThat(repository.getUserIdForApiKey("unknown")).isEmpty();
        assertThat(repository.getApiKeys(BOB)).isEmpty();
    }

    @Test
    void shouldReplaceAKeyWithTheSameId() {
        repository.addApiKey(ALICE, key(K1, "hash-1", "CI"));
        repository.addApiKey(ALICE, key(K1, "hash-3", "Rotated"));

        assertThat(repository.getApiKeys(ALICE)).singleElement().satisfies(record -> {
            assertThat(record.apiKey()).isEqualTo("hash-3");
            assertThat(record.purpose()).isEqualTo("Rotated");
        });
        assertThat(repository.getUserIdForApiKey("hash-1")).isEmpty();
    }

    @Test
    void shouldDropOneOrAllKeys() {
        repository.addApiKey(ALICE, key(K1, "hash-1", "CI"));
        repository.addApiKey(ALICE, key(K2, "hash-2", "Notebook"));

        repository.dropApiKey(ALICE, K1);
        assertThat(repository.getApiKeys(ALICE)).extracting(ApiKeyRecord::getApiKeyId).containsExactly(K2);

        repository.dropApiKeys(ALICE);
        assertThat(repository.getApiKeys(ALICE)).isEmpty();
    }

    @Test
    void shouldSetKeysKeepingTheFirstOfEachId() {
        repository.setApiKeys(BOB, List.of(key(K1, "hash-1", "first"), key(K1, "hash-x", "second"),
                                           key(K2, "hash-2", "other")));

        assertThat(repository.getApiKeys(BOB)).extracting(ApiKeyRecord::purpose).containsExactly("first", "other");
    }

    private static ApiKeyRecord key(ApiKeyId id, String hash, String purpose) {
        return ApiKeyRecord.of(id, hash, 1000, purpose);
    }
}
