package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.issues.Comment;
import org.industrial.ontology.domain.issues.ThreadId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.CommentUpdatedEvent}.
 */
@JsonTypeName("CommentUpdated")
public record CommentUpdatedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("threadId") ThreadId threadId,
        @JsonProperty("comment") Comment comment) implements ProjectEvent {

    public CommentUpdatedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(threadId, "threadId");
        Objects.requireNonNull(comment, "comment");
    }

    public ThreadId getThreadId() {
        return threadId;
    }

    public Comment getComment() {
        return comment;
    }
}
