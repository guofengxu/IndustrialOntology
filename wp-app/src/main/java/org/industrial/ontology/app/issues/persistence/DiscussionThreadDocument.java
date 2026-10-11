package org.industrial.ontology.app.issues.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.CommentId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * A document of the {@code EntityDiscussionThreads} collection, as Morphia wrote the legacy
 * {@code EntityDiscussionThread}: {@code {_id: threadId, projectId, entity: {type, iri}, status, comments: [...]}}.
 * The entity is stored by {@link org.industrial.ontology.app.persistence.OwlEntityMongoCodec}.
 *
 * @param id the thread id, stored as {@code _id}
 */
@Document(DiscussionThreadDocument.COLLECTION)
public record DiscussionThreadDocument(@Id @Nonnull String id,
                                       @Nonnull String projectId,
                                       @Nonnull OWLEntity entity,
                                       @Nonnull Status status,
                                       @Nonnull List<CommentDocument> comments) {

    public static final String COLLECTION = "EntityDiscussionThreads";

    public static final String PROJECT_ID = "projectId";

    public static final String ENTITY = "entity";

    public static final String STATUS = "status";

    public static final String COMMENTS = "comments";

    public static final String COMMENTS_ID = "comments._id";

    public DiscussionThreadDocument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(status, "status");
        comments = comments == null ? List.of() : List.copyOf(comments);
    }

    @Nonnull
    public static DiscussionThreadDocument of(@Nonnull EntityDiscussionThread thread) {
        return new DiscussionThreadDocument(thread.getId().getId(),
                                            thread.getProjectId().getId(),
                                            thread.getEntity(),
                                            thread.getStatus(),
                                            thread.getComments().stream().map(CommentDocument::of).toList());
    }

    @Nonnull
    public EntityDiscussionThread toThread() {
        return new EntityDiscussionThread(new ThreadId(id),
                                          ProjectId.get(projectId),
                                          entity,
                                          status,
                                          comments.stream().map(CommentDocument::toComment).collect(toImmutableList()));
    }

    /**
     * An embedded comment, as Morphia wrote the legacy {@code Comment}: its id is stored as {@code _id}, the
     * timestamps are epoch milliseconds (Int64), and {@code updatedAt} is left out until the comment is edited.
     *
     * @param id           the comment id, stored as {@code _id}
     * @param renderedBody the HTML rendering of the body; {@code null} in documents that never had one
     */
    public record CommentDocument(@Id @Nonnull String id,
                                  @Nonnull String createdBy,
                                  long createdAt,
                                  @Nullable Long updatedAt,
                                  @Nonnull String body,
                                  @Nullable String renderedBody) {

        public CommentDocument {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(createdBy, "createdBy");
            Objects.requireNonNull(body, "body");
        }

        @Nonnull
        public static CommentDocument of(@Nonnull Comment comment) {
            return new CommentDocument(comment.getId().getId(),
                                       comment.getCreatedBy().getUserName(),
                                       comment.getCreatedAt(),
                                       comment.getUpdatedAt().orElse(null),
                                       comment.getBody(),
                                       comment.getRenderedBody());
        }

        /**
         * The comment; a missing rendering becomes the body, which is what the legacy {@code Comment} returned for
         * it.
         */
        @Nonnull
        public Comment toComment() {
            return new Comment(CommentId.fromString(id),
                               UserId.getUserId(createdBy),
                               createdAt,
                               Optional.ofNullable(updatedAt),
                               body,
                               renderedBody == null ? body : renderedBody);
        }
    }
}
