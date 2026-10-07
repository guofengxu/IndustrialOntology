package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.UserStoppedViewingProjectEvent}.
 */
@JsonTypeName("UserStoppedViewingProject")
public record UserStoppedViewingProjectEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("userId") UserId userId) implements ProjectEvent {

    public UserStoppedViewingProjectEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(userId, "userId");
    }

    public UserId getUserId() {
        return userId;
    }
}
