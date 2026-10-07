package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.entity.EntityNode;

import java.util.Objects;

/**
 * Moves (or, with {@link DropType#COPY}, adds a parent to) a node of an entity hierarchy.
 * <p>
 * Replaces {@code edu.stanford.bmir.protege.web.shared.hierarchy.MoveHierarchyNodeAction}: the dispatch action was
 * also the parameter object of the kernel's {@code MoveEntityChangeListGenerator}, and dispatch actions are not
 * ported (docs/01 §4).
 *
 * @param fromNodePath     path from a root to the node being moved
 * @param toNodeParentPath path from a root to the new parent; empty to make the node a root
 */
public record MoveHierarchyNodeRequest(@JsonProperty("projectId") ProjectId projectId,
                                       @JsonProperty("hierarchyId") HierarchyId hierarchyId,
                                       @JsonProperty("fromNodePath") Path<EntityNode> fromNodePath,
                                       @JsonProperty("toNodeParentPath") Path<EntityNode> toNodeParentPath,
                                       @JsonProperty("dropType") DropType dropType) {

    public MoveHierarchyNodeRequest {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(hierarchyId, "hierarchyId");
        Objects.requireNonNull(fromNodePath, "fromNodePath");
        Objects.requireNonNull(toNodeParentPath, "toNodeParentPath");
        Objects.requireNonNull(dropType, "dropType");
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public HierarchyId getHierarchyId() {
        return hierarchyId;
    }

    public Path<EntityNode> getFromNodePath() {
        return fromNodePath;
    }

    public Path<EntityNode> getToNodeParentPath() {
        return toNodeParentPath;
    }

    public DropType getDropType() {
        return dropType;
    }
}
