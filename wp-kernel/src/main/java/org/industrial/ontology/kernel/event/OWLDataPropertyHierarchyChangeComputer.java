package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.api.hierarchy.DataPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.HierarchyChangeComputer;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import java.util.Collection;

import java.util.Collections;
import static org.industrial.ontology.domain.hierarchy.HierarchyId.DATA_PROPERTY_HIERARCHY;
import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;

import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.AddEdge;
import org.industrial.ontology.domain.hierarchy.GraphEdge;
import org.industrial.ontology.domain.hierarchy.RemoveEdge;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.OWLDataPropertyHierarchyChangeComputer}.
 * <p>
* Matthew Horridge
* Stanford Center for Biomedical Informatics Research
* 22/05/15
*/
public class OWLDataPropertyHierarchyChangeComputer extends HierarchyChangeComputer<OWLDataProperty> {

    private final DataPropertyHierarchyProvider hierarchyProvider;

    private final EntityNodeRenderer renderer;

    public OWLDataPropertyHierarchyChangeComputer(ProjectId projectId, DataPropertyHierarchyProvider hierarchyProvider, EntityNodeRenderer renderer) {
        super(projectId, EntityType.DATA_PROPERTY, hierarchyProvider, DATA_PROPERTY_HIERARCHY, renderer);
        this.hierarchyProvider = hierarchyProvider;
        this.renderer = renderer;
    }

    @Override
    protected Collection<? extends ProjectEvent> createRemovedEvents(OWLDataProperty child, OWLDataProperty parent) {
        RemoveEdge<EntityNode> removeEdge = new RemoveEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent)),
                new GraphNode<>(renderer.render(child))
        ));
        return Collections.singletonList(
                new EntityHierarchyChangedEvent(getProjectId(), DATA_PROPERTY_HIERARCHY, new GraphModelChangedEvent<>(Collections.singletonList(removeEdge)))
        );
    }

    @Override
    protected Collection<? extends ProjectEvent> createAddedEvents(OWLDataProperty child, OWLDataProperty parent) {
        AddEdge<EntityNode> addEdge = new AddEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent), hierarchyProvider.isLeaf(parent)),
                new GraphNode<>(renderer.render(child), hierarchyProvider.isLeaf(child))
        ));
        return Collections.singletonList(
                new EntityHierarchyChangedEvent(getProjectId(), DATA_PROPERTY_HIERARCHY, new GraphModelChangedEvent<>(Collections.singletonList(addEdge)))
        );
    }
}
