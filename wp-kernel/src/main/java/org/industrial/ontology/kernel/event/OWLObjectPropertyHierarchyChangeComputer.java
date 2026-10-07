package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.hierarchy.HierarchyChangeComputer;
import org.industrial.ontology.kernel.api.hierarchy.ObjectPropertyHierarchyProvider;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import static org.industrial.ontology.domain.hierarchy.HierarchyId.OBJECT_PROPERTY_HIERARCHY;
import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;

import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.AddEdge;
import org.industrial.ontology.domain.hierarchy.GraphEdge;
import org.industrial.ontology.domain.hierarchy.RemoveEdge;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.OWLObjectPropertyHierarchyChangeComputer}.
 * <p>
* Matthew Horridge
* Stanford Center for Biomedical Informatics Research
* 22/05/15
*/
public class OWLObjectPropertyHierarchyChangeComputer extends HierarchyChangeComputer<OWLObjectProperty> {

    @Nonnull
    private final ObjectPropertyHierarchyProvider hierarchyProvider;

    @Nonnull
    private final EntityNodeRenderer renderer;

    public OWLObjectPropertyHierarchyChangeComputer(@Nonnull ProjectId projectId,
                                                    @Nonnull ObjectPropertyHierarchyProvider hierarchyProvider,
                                                    @Nonnull EntityNodeRenderer renderer) {
        super(projectId, EntityType.OBJECT_PROPERTY, hierarchyProvider, OBJECT_PROPERTY_HIERARCHY, renderer);
        this.hierarchyProvider = hierarchyProvider;
        this.renderer = renderer;
    }

    @Override
    protected Collection<? extends ProjectEvent> createRemovedEvents(OWLObjectProperty child, OWLObjectProperty parent) {
        RemoveEdge<EntityNode> removeEdge = new RemoveEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent)),
                new GraphNode<>(renderer.render(child))
        ));
        return Collections.singletonList(new EntityHierarchyChangedEvent(getProjectId(),
                                                                         OBJECT_PROPERTY_HIERARCHY,
                                                                         new GraphModelChangedEvent<>(Collections.singletonList(
                                                                                 removeEdge))));
    }

    @Override
    protected Collection<? extends ProjectEvent> createAddedEvents(OWLObjectProperty child, OWLObjectProperty parent) {
        AddEdge<EntityNode> addEdge = new AddEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent), hierarchyProvider.isLeaf(parent)),
                new GraphNode<>(renderer.render(child), hierarchyProvider.isLeaf(child))
        ));
        return Collections.singletonList(new EntityHierarchyChangedEvent(getProjectId(),
                                                                         OBJECT_PROPERTY_HIERARCHY,
                                                                         new GraphModelChangedEvent<>(Collections.singletonList(
                                                                                 addEdge))));
    }
}
