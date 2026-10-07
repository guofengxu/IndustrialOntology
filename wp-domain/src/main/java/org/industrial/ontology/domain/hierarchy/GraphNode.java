package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A node of a hierarchy graph.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.GraphNode} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 *
 * @param userObject the node payload, typically an {@link org.industrial.ontology.domain.entity.EntityNode}
 * @param sink       {@code true} when the node has no successors
 */
public record GraphNode<U>(@JsonProperty("userObject") U userObject, @JsonProperty("sink") boolean sink) {

    public GraphNode {
        Objects.requireNonNull(userObject, "userObject");
    }

    public GraphNode(U userObject) {
        this(userObject, false);
    }

    @JsonProperty("userObject")
    public U getUserObject() {
        return userObject;
    }

    @JsonProperty("sink")
    public boolean isSink() {
        return sink;
    }
}
