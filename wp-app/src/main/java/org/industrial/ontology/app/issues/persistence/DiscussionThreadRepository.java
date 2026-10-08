package org.industrial.ontology.app.issues.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.CommentId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument.COMMENTS;
import static org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument.COMMENTS_ID;
import static org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument.ENTITY;
import static org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument.PROJECT_ID;
import static org.industrial.ontology.app.issues.persistence.DiscussionThreadDocument.STATUS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code EntityDiscussionThreads} collection; ported from the legacy {@code EntityDiscussionThreadRepository}.
 * It is also the kernel's {@link EntityDiscussionThreadRepository} port.
 */
public class DiscussionThreadRepository implements EntityDiscussionThreadRepository {

    private static final String MATCHED_COMMENT = COMMENTS + ".$";

    private static final Comparator<DiscussionThreadDocument> mostRecentlyStartedFirst = Comparator.comparingLong(
            (DiscussionThreadDocument thread) -> thread.comments().isEmpty()
                    ? Long.MIN_VALUE : thread.comments().get(0).createdAt()).reversed();

    private final MongoOperations mongo;

    public DiscussionThreadRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * The threads about the entity, the most recently started first. The legacy repository had Mongo sort by
     * {@code -comments.0.createdAt}; an entity has few threads, so they are sorted here the same way, threads without
     * comments last.
     */
    @Nonnull
    public List<EntityDiscussionThread> findThreads(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
        return mongo.find(byEntity(projectId, entity), DiscussionThreadDocument.class)
                    .stream()
                    .sorted(mostRecentlyStartedFirst)
                    .map(DiscussionThreadDocument::toThread)
                    .toList();
    }

    public int getCommentsCount(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
        return countComments(byEntity(projectId, entity));
    }

    @Override
    public int getOpenCommentsCount(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
        return countComments(byEntity(projectId, entity).addCriteria(where(STATUS).is(Status.OPEN)));
    }

    /**
     * Inserts the thread, or replaces the stored thread with the same id.
     */
    public void saveThread(@Nonnull EntityDiscussionThread thread) {
        mongo.save(DiscussionThreadDocument.of(thread));
    }

    public void addCommentToThread(@Nonnull ThreadId threadId, @Nonnull Comment comment) {
        mongo.updateFirst(byThreadId(threadId),
                          new Update().push(COMMENTS, DiscussionThreadDocument.CommentDocument.of(comment)),
                          DiscussionThreadDocument.class);
    }

    @Nonnull
    public Optional<EntityDiscussionThread> setThreadStatus(@Nonnull ThreadId threadId, @Nonnull Status status) {
        mongo.updateFirst(byThreadId(threadId), new Update().set(STATUS, status), DiscussionThreadDocument.class);
        return getThread(threadId);
    }

    @Nonnull
    public Optional<EntityDiscussionThread> getThread(@Nonnull ThreadId threadId) {
        return Optional.ofNullable(mongo.findOne(byThreadId(threadId), DiscussionThreadDocument.class))
                       .map(DiscussionThreadDocument::toThread);
    }

    /**
     * Re-points the project's threads about {@code entity} at {@code withEntity}, used when entities are merged.
     */
    @Override
    public void replaceEntity(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity, @Nonnull OWLEntity withEntity) {
        mongo.updateMulti(byEntity(projectId, entity), new Update().set(ENTITY, withEntity),
                          DiscussionThreadDocument.class);
    }

    /**
     * Replaces the stored comment that has the same id.
     */
    public void updateComment(@Nonnull ThreadId threadId, @Nonnull Comment comment) {
        var query = byThreadId(threadId).addCriteria(where(COMMENTS_ID).is(comment.getId().getId()));
        mongo.updateFirst(query,
                          new Update().set(MATCHED_COMMENT, DiscussionThreadDocument.CommentDocument.of(comment)),
                          DiscussionThreadDocument.class);
    }

    @Nonnull
    public Optional<EntityDiscussionThread> findThreadByCommentId(@Nonnull CommentId commentId) {
        var query = Query.query(where(COMMENTS_ID).is(commentId.getId()));
        return Optional.ofNullable(mongo.findOne(query, DiscussionThreadDocument.class))
                       .map(DiscussionThreadDocument::toThread);
    }

    /**
     * @return whether a comment was deleted
     */
    public boolean deleteComment(@Nonnull CommentId commentId) {
        var query = Query.query(where(COMMENTS_ID).is(commentId.getId()));
        var update = new Update().pull(COMMENTS, new org.bson.Document("_id", commentId.getId()));
        return mongo.updateFirst(query, update, DiscussionThreadDocument.class).getModifiedCount() == 1;
    }

    @Nonnull
    public List<EntityDiscussionThread> getThreadsInProject(@Nonnull ProjectId projectId) {
        return mongo.find(Query.query(where(PROJECT_ID).is(projectId.getId())), DiscussionThreadDocument.class)
                    .stream()
                    .map(DiscussionThreadDocument::toThread)
                    .toList();
    }

    private int countComments(Query query) {
        return mongo.find(query, DiscussionThreadDocument.class)
                    .stream()
                    .mapToInt(thread -> thread.comments().size())
                    .sum();
    }

    private static Query byEntity(ProjectId projectId, OWLEntity entity) {
        return Query.query(where(PROJECT_ID).is(projectId.getId()).and(ENTITY).is(checkNotNull(entity)));
    }

    private static Query byThreadId(ThreadId threadId) {
        return Query.query(where("_id").is(threadId.getId()));
    }
}
