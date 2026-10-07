package org.industrial.ontology.domain.hierarchy;

/**
 * Visitor over {@link GraphModelChange}s.
 * <p>
 * Replaces {@code edu.stanford.protege.gwt.graphtree.shared.graph.GraphModelChangeVisitor} from the GWT graphtree library, which the
 * legacy server used only as a data carrier for hierarchy change events.
 */
public interface GraphModelChangeVisitor<U> {

    void visit(AddRootNode<U> addRootNode);

    void visit(RemoveRootNode<U> removeRootNode);

    void visit(AddEdge<U> addEdge);

    void visit(RemoveEdge<U> removeEdge);

    void visit(UpdateUserObject<U> updateUserObject);
}
