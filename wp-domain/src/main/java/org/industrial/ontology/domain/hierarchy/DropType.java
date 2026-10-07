package org.industrial.ontology.domain.hierarchy;

/**
 * Whether dragging a hierarchy node moves it to the new parent or adds the new parent.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.DropType} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public enum DropType {
    MOVE,
    COPY
}
