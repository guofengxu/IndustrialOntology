package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;
import org.industrial.ontology.domain.hierarchy.HierarchyId;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.hierarchy.EntityHierarchyChangedEvent}.
 */
@JsonTypeName("EntityHierarchyChanged")
public record EntityHierarchyChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("hierarchyId") HierarchyId hierarchyId,
        @JsonProperty("changeEvent") GraphModelChangedEvent<EntityNode> changeEvent) implements ProjectEvent {

    public EntityHierarchyChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(hierarchyId, "hierarchyId");
        Objects.requireNonNull(changeEvent, "changeEvent");
    }

    public HierarchyId getHierarchyId() {
        return hierarchyId;
    }

    public GraphModelChangedEvent<EntityNode> getChangeEvent() {
        return changeEvent;
    }
}
