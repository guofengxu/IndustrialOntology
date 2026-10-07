package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * One structural change to a hierarchy graph; clients apply these to update trees incrementally.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.GraphModelChange} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AddRootNode.class, name = "AddRootNode"),
        @JsonSubTypes.Type(value = RemoveRootNode.class, name = "RemoveRootNode"),
        @JsonSubTypes.Type(value = AddEdge.class, name = "AddEdge"),
        @JsonSubTypes.Type(value = RemoveEdge.class, name = "RemoveEdge"),
        @JsonSubTypes.Type(value = UpdateUserObject.class, name = "UpdateUserObject")})
public sealed interface GraphModelChange<U> permits AddRootNode, RemoveRootNode, AddEdge, RemoveEdge, UpdateUserObject {

    void accept(GraphModelChangeVisitor<U> visitor);
}
