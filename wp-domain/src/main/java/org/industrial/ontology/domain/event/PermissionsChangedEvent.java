package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.permissions.PermissionsChangedEvent}.
 */
@JsonTypeName("PermissionsChanged")
public record PermissionsChangedEvent(@JsonProperty("projectId") ProjectId projectId) implements ProjectEvent {

    public PermissionsChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
    }
}
