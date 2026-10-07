package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A parent-child edge was removed.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.RemoveEdge} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record RemoveEdge<U>(@JsonProperty("edge") GraphEdge<U> edge) implements GraphModelChange<U> {

    public RemoveEdge {
        Objects.requireNonNull(edge, "edge");
    }

    @JsonProperty("edge")
    public GraphEdge<U> getEdge() {
        return edge;
    }

    @Override
    public void accept(GraphModelChangeVisitor<U> visitor) {
        visitor.visit(this);
    }
}
