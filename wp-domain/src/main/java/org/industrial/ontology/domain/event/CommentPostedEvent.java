package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.ThreadId;

import java.util.Objects;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.CommentPostedEvent}.
 */
@JsonTypeName("CommentPosted")
public record CommentPostedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("threadId") ThreadId threadId,
        @JsonProperty("comment") Comment comment,
        @JsonProperty("entity") Optional<OWLEntityData> entity,
        @JsonProperty("commentCountForEntity") int commentCountForEntity,
        @JsonProperty("openCommentCountForEntity") int openCommentCountForEntity) implements ProjectEvent {

    public CommentPostedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(threadId, "threadId");
        Objects.requireNonNull(comment, "comment");
        Objects.requireNonNull(entity, "entity");
    }

    public ThreadId getThreadId() {
        return threadId;
    }

    public Comment getComment() {
        return comment;
    }

    public Optional<OWLEntityData> getEntity() {
        return entity;
    }

    public int getCommentCountForEntity() {
        return commentCountForEntity;
    }

    public int getOpenCommentCountForEntity() {
        return openCommentCountForEntity;
    }
}
