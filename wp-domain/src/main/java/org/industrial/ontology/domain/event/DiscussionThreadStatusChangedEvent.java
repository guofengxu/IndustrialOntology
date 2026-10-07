package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.issues.Status;
import org.industrial.ontology.domain.issues.ThreadId;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Objects;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.DiscussionThreadStatusChangedEvent}.
 */
@JsonTypeName("DiscussionThreadStatusChanged")
public record DiscussionThreadStatusChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("threadId") ThreadId threadId,
        @JsonProperty("entity") Optional<OWLEntity> entity,
        @JsonProperty("openCommentsCountForEntity") int openCommentsCountForEntity,
        @JsonProperty("status") Status status) implements ProjectEvent {

    public DiscussionThreadStatusChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(threadId, "threadId");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(status, "status");
    }

    public ThreadId getThreadId() {
        return threadId;
    }

    public Optional<OWLEntity> getEntity() {
        return entity;
    }

    public int getOpenCommentsCountForEntity() {
        return openCommentsCountForEntity;
    }

    public Status getStatus() {
        return status;
    }
}
