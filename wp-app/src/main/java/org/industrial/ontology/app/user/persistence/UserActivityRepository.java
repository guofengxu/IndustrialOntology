package org.industrial.ontology.app.user.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code UserActivity} collection; ported from the legacy {@code UserActivityManager}. Nothing is recorded for
 * the guest user.
 */
public class UserActivityRepository {

    private final MongoOperations mongo;

    public UserActivityRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    public void save(@Nonnull UserActivityDocument record) {
        if (record.getUserId().isGuest()) {
            return;
        }
        mongo.save(record);
    }

    @Nonnull
    public Optional<UserActivityDocument> getUserActivityRecord(@Nonnull UserId userId) {
        if (userId.isGuest()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mongo.findById(userId.getUserName(), UserActivityDocument.class));
    }

    public void setLastLogin(@Nonnull UserId userId, long lastLogin) {
        setTimestamp(userId, UserActivityDocument.LAST_LOGIN, UserActivityDocument.LAST_LOGOUT, lastLogin);
    }

    public void setLastLogout(@Nonnull UserId userId, long lastLogout) {
        setTimestamp(userId, UserActivityDocument.LAST_LOGOUT, UserActivityDocument.LAST_LOGIN, lastLogout);
    }

    /**
     * Sets one timestamp. Like the legacy manager, a user without a record gets a complete one, with the other
     * timestamp unknown and no recent projects; here that is a single upsert instead of an insert and an update.
     */
    private void setTimestamp(UserId userId, String field, String otherField, long timestamp) {
        if (userId.isGuest()) {
            return;
        }
        var update = new Update().set(field, Instant.ofEpochMilli(timestamp))
                                 .setOnInsert(otherField, Instant.EPOCH)
                                 .setOnInsert(UserActivityDocument.RECENT_PROJECTS, List.of());
        mongo.upsert(byUserId(userId), update, UserActivityDocument.class);
    }

    /**
     * Moves the project to the front of the user's recent projects, as the legacy manager did: the other entries keep
     * their {@link UserActivityDocument.RecentProject} order and the list is not truncated.
     */
    public void addRecentProject(@Nonnull UserId userId, @Nonnull ProjectId projectId, long timestamp) {
        if (userId.isGuest()) {
            return;
        }
        var record = getUserActivityRecord(userId).orElseGet(() -> UserActivityDocument.empty(userId));
        var recentProjects = new ArrayList<UserActivityDocument.RecentProject>();
        recentProjects.add(UserActivityDocument.RecentProject.of(projectId, timestamp));
        record.recentProjects()
              .stream()
              .filter(recentProject -> !recentProject.projectId().equals(projectId.getId()))
              .sorted()
              .forEach(recentProjects::add);
        save(new UserActivityDocument(record.userId(), record.lastLogin(), record.lastLogout(), recentProjects));
    }

    private static Query byUserId(UserId userId) {
        return Query.query(where("_id").is(userId.getUserName()));
    }
}
