package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A parent-to-child edge of a hierarchy graph.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.GraphEdge} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record GraphEdge<U>(@JsonProperty("predecessor") GraphNode<U> predecessor,
                           @JsonProperty("successor") GraphNode<U> successor) {

    public GraphEdge {
        Objects.requireNonNull(predecessor, "predecessor");
        Objects.requireNonNull(successor, "successor");
    }

    @JsonProperty("predecessor")
    public GraphNode<U> getPredecessor() {
        return predecessor;
    }

    @JsonProperty("successor")
    public GraphNode<U> getSuccessor() {
        return successor;
    }
}
