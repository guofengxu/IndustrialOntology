package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.ProjectMovedToTrashEvent}.
 */
@JsonTypeName("ProjectMovedToTrash")
public record ProjectMovedToTrashEvent(@JsonProperty("projectId") ProjectId projectId) implements ProjectEvent {

    public ProjectMovedToTrashEvent {
        Objects.requireNonNull(projectId, "projectId");
    }
}
