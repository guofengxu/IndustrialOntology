package org.industrial.ontology.app.access.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code RoleAssignments} collection: the queries of the legacy {@code AccessManagerImpl}. The access manager
 * itself, which turns subjects and resources into these arguments and computes the closures, is ported in S5.
 * <p>
 * In every method a {@code null} user name is the "any signed-in user" subject and a {@code null} project id is the
 * application resource. Mongo matches {@code null} against missing fields, which is how those assignments are
 * stored.
 */
public class RoleAssignmentRepository {

    private final MongoOperations mongo;

    public RoleAssignmentRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * Replaces the subject's assignment on the resource. The legacy manager deleted the old document and inserted a
     * new one; replacing it keeps its {@code _id}, and the unique index on {@code (userName, projectId)} means there
     * is at most one to replace.
     */
    public void setAssignedRoles(@Nullable String userName,
                                 @Nullable String projectId,
                                 @Nonnull List<String> assignedRoles,
                                 @Nonnull List<String> roleClosure,
                                 @Nonnull List<String> actionClosure) {
        save(new RoleAssignmentDocument(null, userName, projectId, assignedRoles, roleClosure, actionClosure));
    }

    /**
     * Inserts the assignment, or replaces the one of the same subject and resource.
     */
    public void save(@Nonnull RoleAssignmentDocument assignment) {
        mongo.findAndReplace(Query.query(subjectOnResource(assignment.userName(), assignment.projectId())),
                             assignment,
                             FindAndReplaceOptions.options().upsert());
    }

    /**
     * The assignments of exactly this subject on the resource.
     */
    @Nonnull
    public List<RoleAssignmentDocument> findAssignments(@Nullable String userName, @Nullable String projectId) {
        return mongo.find(Query.query(subjectOnResource(userName, projectId)), RoleAssignmentDocument.class);
    }

    /**
     * The assignments that apply to a user on the resource: the user's own and, unless the user is the guest, those
     * of any signed-in user.
     */
    @Nonnull
    public List<RoleAssignmentDocument> findApplicableAssignments(@Nullable String userName,
                                                                  boolean includeAnySignedInUser,
                                                                  @Nullable String projectId) {
        return mongo.find(applicable(userName, includeAnySignedInUser, projectId), RoleAssignmentDocument.class);
    }

    /**
     * Whether any applicable assignment (see {@link #findApplicableAssignments}) includes the action.
     */
    public boolean hasAction(@Nullable String userName,
                             boolean includeAnySignedInUser,
                             @Nullable String projectId,
                             @Nonnull String actionId) {
        var query = applicable(userName, includeAnySignedInUser, projectId);
        query.addCriteria(where(RoleAssignmentDocument.ACTION_CLOSURE).is(checkNotNull(actionId)));
        return mongo.exists(query, RoleAssignmentDocument.class);
    }

    /**
     * Every assignment on the resource, whoever the subject.
     */
    @Nonnull
    public List<RoleAssignmentDocument> findByProjectId(@Nullable String projectId) {
        return mongo.find(Query.query(where(RoleAssignmentDocument.PROJECT_ID).is(projectId)),
                          RoleAssignmentDocument.class);
    }

    /**
     * The subject's assignments, on any resource, that include the action.
     */
    @Nonnull
    public List<RoleAssignmentDocument> findByUserNameAndAction(@Nullable String userName, @Nonnull String actionId) {
        var query = Query.query(where(RoleAssignmentDocument.USER_NAME).is(userName)
                                        .and(RoleAssignmentDocument.ACTION_CLOSURE).is(checkNotNull(actionId)));
        return mongo.find(query, RoleAssignmentDocument.class);
    }

    @Nonnull
    public List<RoleAssignmentDocument> findAll() {
        return mongo.findAll(RoleAssignmentDocument.class);
    }

    /**
     * Stores recomputed closures, for rebuild-permissions.
     */
    public void setClosures(@Nonnull ObjectId id, @Nonnull List<String> roleClosure, @Nonnull List<String> actionClosure) {
        var update = new Update().set(RoleAssignmentDocument.ACTION_CLOSURE, actionClosure)
                                 .set(RoleAssignmentDocument.ROLE_CLOSURE, roleClosure);
        mongo.updateFirst(Query.query(where("_id").is(id)), update, RoleAssignmentDocument.class);
    }

    private static Criteria subjectOnResource(@Nullable String userName, @Nullable String projectId) {
        return where(RoleAssignmentDocument.USER_NAME).is(userName)
                .and(RoleAssignmentDocument.PROJECT_ID).is(projectId);
    }

    private static Query applicable(@Nullable String userName, boolean includeAnySignedInUser, @Nullable String projectId) {
        var subject = includeAnySignedInUser
                ? new Criteria().orOperator(where(RoleAssignmentDocument.USER_NAME).is(userName),
                                            where(RoleAssignmentDocument.USER_NAME).is(null))
                : where(RoleAssignmentDocument.USER_NAME).is(userName);
        return Query.query(new Criteria().andOperator(where(RoleAssignmentDocument.PROJECT_ID).is(projectId), subject));
    }
}
