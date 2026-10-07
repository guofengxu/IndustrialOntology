package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.issues.EntityDiscussionThread;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.DiscussionThreadCreatedEvent}.
 */
@JsonTypeName("DiscussionThreadCreated")
public record DiscussionThreadCreatedEvent(@JsonProperty("thread") EntityDiscussionThread thread) implements ProjectEvent {

    public DiscussionThreadCreatedEvent {
        Objects.requireNonNull(thread, "thread");
    }

    @Override
    public ProjectId projectId() {
        return thread.getProjectId();
    }

    public EntityDiscussionThread getThread() {
        return thread;
    }
}
