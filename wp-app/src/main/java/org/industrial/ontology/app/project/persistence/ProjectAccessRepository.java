package org.industrial.ontology.app.project.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code ProjectAccess} collection; ported from the legacy {@code ProjectAccessManagerImpl}.
 */
public class ProjectAccessRepository {

    private final MongoOperations mongo;

    public ProjectAccessRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * Counts one more access of the project by the user and records its time, creating the document on the first
     * access.
     */
    public void logProjectAccess(@Nonnull ProjectId projectId, @Nonnull UserId userId, long timestamp) {
        var update = new Update().inc(ProjectAccessDocument.COUNT, 1)
                                 .set(ProjectAccessDocument.ACCESSED, Instant.ofEpochMilli(timestamp));
        mongo.upsert(byProjectAndUser(projectId, userId), update, ProjectAccessDocument.class);
    }

    @Nonnull
    public Optional<ProjectAccessDocument> findAccess(@Nonnull ProjectId projectId, @Nonnull UserId userId) {
        return Optional.ofNullable(mongo.findOne(byProjectAndUser(projectId, userId), ProjectAccessDocument.class));
    }

    private static Query byProjectAndUser(ProjectId projectId, UserId userId) {
        return Query.query(where(ProjectAccessDocument.PROJECT_ID).is(projectId.getId())
                                   .and(ProjectAccessDocument.USER_ID).is(userId.getUserName()));
    }
}
