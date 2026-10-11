package org.industrial.ontology.app.apikey.persistence;

import org.industrial.ontology.domain.core.ApiKeyId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * A document of the {@code UserApiKeys} collection, as Morphia wrote the legacy {@code UserApiKeys}:
 * {@code {_id: userName, apiKeys: [{apiKeyId, apiKey, createdAt: Date, purpose}]}}. Only the SHA-256 hash of a key
 * (lower-case hex) is stored; key ids and hashes are unique across users.
 *
 * @param userId stored as {@code _id}
 */
@Document(UserApiKeysDocument.COLLECTION)
public record UserApiKeysDocument(@Id @Nonnull String userId, @Nonnull List<ApiKeyRecord> apiKeys) {

    public static final String COLLECTION = "UserApiKeys";

    public static final String API_KEYS = "apiKeys";

    public static final String API_KEYS_API_KEY = API_KEYS + ".apiKey";

    public UserApiKeysDocument {
        Objects.requireNonNull(userId, "userId");
        apiKeys = apiKeys == null ? List.of() : List.copyOf(apiKeys);
    }

    /**
     * One key of a user, as the legacy {@code ApiKeyRecord}.
     *
     * @param apiKey the SHA-256 hash of the key, lower-case hex
     */
    public record ApiKeyRecord(@Nonnull String apiKeyId,
                               @Nonnull String apiKey,
                               @Nonnull Instant createdAt,
                               @Nonnull String purpose) {

        public static final String API_KEY_ID = "apiKeyId";

        public ApiKeyRecord {
            Objects.requireNonNull(apiKeyId, "apiKeyId");
            Objects.requireNonNull(apiKey, "apiKey");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(purpose, "purpose");
        }

        @Nonnull
        public static ApiKeyRecord of(@Nonnull ApiKeyId apiKeyId,
                                      @Nonnull String hashedApiKey,
                                      long createdAt,
                                      @Nonnull String purpose) {
            return new ApiKeyRecord(apiKeyId.getId(), hashedApiKey, Instant.ofEpochMilli(createdAt), purpose);
        }

        @Nonnull
        public ApiKeyId getApiKeyId() {
            return ApiKeyId.valueOf(apiKeyId);
        }
    }
}
