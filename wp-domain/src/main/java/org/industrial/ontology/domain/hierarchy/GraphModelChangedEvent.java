package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * An ordered batch of {@link GraphModelChange}s produced by one revision.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.GraphModelChangedEvent} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record GraphModelChangedEvent<U>(@JsonProperty("changes") List<GraphModelChange<U>> changes) {

    public GraphModelChangedEvent {
        changes = List.copyOf(changes);
    }

    @JsonProperty("changes")
    public List<GraphModelChange<U>> getChanges() {
        return changes;
    }
}
