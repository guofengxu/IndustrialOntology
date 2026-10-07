package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.UserStartingViewingProjectEvent}.
 */
@JsonTypeName("UserStartedViewingProject")
public record UserStartingViewingProjectEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("userId") UserId userId) implements ProjectEvent {

    public UserStartingViewingProjectEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(userId, "userId");
    }

    public UserId getUserId() {
        return userId;
    }
}
