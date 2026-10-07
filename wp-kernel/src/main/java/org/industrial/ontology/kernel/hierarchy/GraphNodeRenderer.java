package org.industrial.ontology.kernel.hierarchy;



import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import org.industrial.ontology.kernel.api.hierarchy.HierarchyProvider;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.GraphNodeRenderer}.
 * <p>
 * Matthew Horridge Stanford Center for Biomedical Informatics Research 19 Dec 2017
 */
public class GraphNodeRenderer {

    @Nonnull
    private final EntityNodeRenderer renderer;

    public GraphNodeRenderer(@Nonnull EntityNodeRenderer renderer) {
        this.renderer = checkNotNull(renderer);
    }

    /**
     * Render the specified entity into a {@link GraphNode} whose user object
     * is and {@link EntityNode}.
     * @param entity The entity to be rendered.
     * @param hierarchyProvider A hierarchy that is used to provide information.
     */
    public GraphNode<EntityNode> toGraphNode(@Nonnull OWLEntity entity,
                                             @Nonnull HierarchyProvider<OWLEntity> hierarchyProvider) {
        return new GraphNode<>(renderer.render(entity), hierarchyProvider.isLeaf(entity));
    }
}
