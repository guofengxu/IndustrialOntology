package org.industrial.ontology.app.apikey.persistence;

import org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument.ApiKeyRecord;
import org.industrial.ontology.domain.core.ApiKeyId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument.API_KEYS;
import static org.industrial.ontology.app.apikey.persistence.UserApiKeysDocument.API_KEYS_API_KEY;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code UserApiKeys} collection; ported from the legacy {@code UserApiKeyStoreImpl}.
 */
public class UserApiKeyRepository {

    private final MongoOperations mongo;

    public UserApiKeyRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * Adds the key at the end of the user's keys, replacing a key with the same id.
     * <p>
     * The legacy store pulled a key with the same id and then added the new one. When that was the user's only key,
     * the pull left an empty {@code apiKeys} array in between, which the unique indexes on {@code apiKeys.*} index as
     * a missing value and can reject; replacing the list in one write gives the same result without that step.
     */
    public void addApiKey(@Nonnull UserId userId, @Nonnull ApiKeyRecord record) {
        var keys = getApiKeys(userId);
        if (keys.stream().noneMatch(key -> key.apiKeyId().equals(record.apiKeyId()))) {
            mongo.upsert(byUserId(userId), new Update().addToSet(API_KEYS, record), UserApiKeysDocument.class);
            return;
        }
        var replaced = new ArrayList<ApiKeyRecord>();
        keys.stream().filter(key -> !key.apiKeyId().equals(record.apiKeyId())).forEach(replaced::add);
        replaced.add(record);
        mongo.save(new UserApiKeysDocument(userId.getUserName(), replaced));
    }

    public void dropApiKeys(@Nonnull UserId userId) {
        mongo.remove(byUserId(userId), UserApiKeysDocument.class);
    }

    public void dropApiKey(@Nonnull UserId userId, @Nonnull ApiKeyId apiKeyId) {
        var update = new Update().pull(API_KEYS, new org.bson.Document(ApiKeyRecord.API_KEY_ID, apiKeyId.getId()));
        mongo.updateFirst(byUserId(userId), update, UserApiKeysDocument.class);
    }

    /**
     * Replaces all of the user's keys; of several keys with the same id only the first is kept. The legacy store set
     * {@code apiKeys}; the document has no other field, so it is replaced as a whole, which also drops anything old
     * Morphia versions left in it.
     */
    public void setApiKeys(@Nonnull UserId userId, @Nonnull List<ApiKeyRecord> records) {
        var ids = new HashSet<String>();
        var nonDuplicates = records.stream().filter(record -> ids.add(record.apiKeyId())).toList();
        mongo.save(new UserApiKeysDocument(userId.getUserName(), nonDuplicates));
    }

    @Nonnull
    public List<ApiKeyRecord> getApiKeys(@Nonnull UserId userId) {
        return Optional.ofNullable(mongo.findById(userId.getUserName(), UserApiKeysDocument.class))
                       .map(UserApiKeysDocument::apiKeys)
                       .orElse(List.of());
    }

    /**
     * The user that the key belongs to.
     *
     * @param hashedApiKey the SHA-256 hash of the key, lower-case hex
     */
    @Nonnull
    public Optional<UserId> getUserIdForApiKey(@Nonnull String hashedApiKey) {
        var query = Query.query(where(API_KEYS_API_KEY).is(checkNotNull(hashedApiKey)));
        query.fields().include("_id");
        return Optional.ofNullable(mongo.findOne(query, org.bson.Document.class, UserApiKeysDocument.COLLECTION))
                       .map(document -> UserId.getUserId(document.getString("_id")));
    }

    private static Query byUserId(UserId userId) {
        return Query.query(where("_id").is(userId.getUserName()));
    }
}
