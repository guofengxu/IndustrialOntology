package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A node stopped being a root of the hierarchy.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.RemoveRootNode} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record RemoveRootNode<U>(@JsonProperty("rootNode") GraphNode<U> rootNode) implements GraphModelChange<U> {

    public RemoveRootNode {
        Objects.requireNonNull(rootNode, "rootNode");
    }

    @JsonProperty("rootNode")
    public GraphNode<U> getRootNode() {
        return rootNode;
    }

    @Override
    public void accept(GraphModelChangeVisitor<U> visitor) {
        visitor.visit(this);
    }
}
