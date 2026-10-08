package org.industrial.ontology.app.user.persistence;

import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code Users} collection; ported from the legacy {@code UserRecordRepository}.
 */
public class UserRecordRepository {

    private static final String USER_ID = "_id";

    private static final String AVATAR = "avatar";

    private final MongoOperations mongo;

    public UserRecordRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    @Nonnull
    public Optional<UserRecordDocument> findOne(@Nonnull UserId userId) {
        return Optional.ofNullable(mongo.findById(userId.getUserName(), UserRecordDocument.class));
    }

    @Nonnull
    public Optional<UserRecordDocument> findOneByEmailAddress(@Nonnull String emailAddress) {
        var query = Query.query(where(UserRecordDocument.EMAIL_ADDRESS).is(checkNotNull(emailAddress))).limit(1);
        return Optional.ofNullable(mongo.findOne(query, UserRecordDocument.class));
    }

    /**
     * The users whose name contains {@code match}, ignoring case.
     * <p>
     * The legacy repository passed {@code match} to Mongo as a regular expression; here it is matched literally, so
     * a name typed into a user picker cannot be an expensive or invalid pattern.
     */
    @Nonnull
    public List<UserId> findByUserIdContainingIgnoreCase(@Nonnull String match, int limit) {
        var query = Query.query(where(USER_ID).regex(Pattern.quote(checkNotNull(match)), "i")).limit(limit);
        query.fields().include(USER_ID);
        return mongo.find(query, org.bson.Document.class, UserRecordDocument.COLLECTION)
                    .stream()
                    .map(document -> UserId.getUserId(document.getString(USER_ID)))
                    .toList();
    }

    /**
     * The users whose name contains {@code match}, ignoring case, in user name order; like
     * {@link #findByUserIdContainingIgnoreCase} but with the whole documents.
     */
    @Nonnull
    public List<UserRecordDocument> findUsersContainingIgnoreCase(@Nonnull String match, int limit) {
        var query = Query.query(where(USER_ID).regex(Pattern.quote(checkNotNull(match)), "i"))
                         .with(Sort.by(USER_ID))
                         .limit(limit);
        return mongo.find(query, UserRecordDocument.class);
    }

    /**
     * Inserts the user unless a user with this name exists, in which case nothing changes; for users seen for the
     * first time in a token (docs/01 §6). It is a single upsert, so two first requests at the same time insert one
     * document, and a user that wp-cli created in the meantime keeps its password.
     *
     * @return whether the user was inserted
     */
    public boolean insertIfAbsent(@Nonnull UserRecordDocument userRecord) {
        var update = new Update().setOnInsert(UserRecordDocument.REAL_NAME, userRecord.realName())
                                 .setOnInsert(UserRecordDocument.EMAIL_ADDRESS, userRecord.emailAddress());
        if (userRecord.avatarUrl() != null) {
            update.setOnInsert(AVATAR, userRecord.avatarUrl());
        }
        var result = mongo.upsert(Query.query(where(USER_ID).is(userRecord.userId())), update,
                                  UserRecordDocument.class);
        return result.getUpsertedId() != null;
    }

    /**
     * Inserts the user, or replaces the stored document with the same user name.
     */
    public void save(@Nonnull UserRecordDocument userRecord) {
        mongo.save(checkNotNull(userRecord));
    }

    public void delete(@Nonnull UserId userId) {
        mongo.remove(Query.query(where(USER_ID).is(userId.getUserName())), UserRecordDocument.class);
    }
}
