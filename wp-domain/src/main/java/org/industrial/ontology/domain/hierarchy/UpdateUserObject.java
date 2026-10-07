package org.industrial.ontology.domain.hierarchy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * The payload of a node changed, for example its rendering.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.UpdateUserObject} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public record UpdateUserObject<U>(@JsonProperty("userObject") U userObject) implements GraphModelChange<U> {

    public UpdateUserObject {
        Objects.requireNonNull(userObject, "userObject");
    }

    @JsonProperty("userObject")
    public U getUserObject() {
        return userObject;
    }

    @Override
    public void accept(GraphModelChangeVisitor<U> visitor) {
        visitor.visit(this);
    }
}
