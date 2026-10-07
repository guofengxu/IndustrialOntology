package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.api.hierarchy.AnnotationPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.HierarchyChangeComputer;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.hierarchy.HierarchyId.ANNOTATION_PROPERTY_HIERARCHY;

import static java.util.Collections.singletonList;
import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.AddEdge;
import org.industrial.ontology.domain.hierarchy.GraphEdge;
import org.industrial.ontology.domain.hierarchy.RemoveEdge;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.OWLAnnotationPropertyHierarchyChangeComputer}.
 * <p>
* Matthew Horridge
* Stanford Center for Biomedical Informatics Research
* 22/05/15
*/
public class OWLAnnotationPropertyHierarchyChangeComputer extends HierarchyChangeComputer<OWLAnnotationProperty> {

    private final EntityNodeRenderer renderer;

    private final AnnotationPropertyHierarchyProvider hierarchyProvider;

    public OWLAnnotationPropertyHierarchyChangeComputer(ProjectId projectId, AnnotationPropertyHierarchyProvider hierarchyProvider, EntityNodeRenderer renderer) {
        super(projectId, EntityType.ANNOTATION_PROPERTY, hierarchyProvider, ANNOTATION_PROPERTY_HIERARCHY, renderer);
        this.renderer = checkNotNull(renderer);
        this.hierarchyProvider = checkNotNull(hierarchyProvider);
    }

    @Override
    protected Collection<? extends ProjectEvent> createRemovedEvents(OWLAnnotationProperty child, OWLAnnotationProperty parent) {
        RemoveEdge<EntityNode> removeEdge = new RemoveEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent)),
                new GraphNode<>(renderer.render(child))
        ));
        return singletonList(
                new EntityHierarchyChangedEvent(getProjectId(), ANNOTATION_PROPERTY_HIERARCHY, new GraphModelChangedEvent<>(singletonList(removeEdge)))
        );
    }

    @Override
    protected Collection<? extends ProjectEvent> createAddedEvents(OWLAnnotationProperty child, OWLAnnotationProperty parent) {
        AddEdge<EntityNode> addEdge = new AddEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent), hierarchyProvider.isLeaf(parent)),
                new GraphNode<>(renderer.render(child), hierarchyProvider.isLeaf(child))
        ));
        return singletonList(
                new EntityHierarchyChangedEvent(getProjectId(), ANNOTATION_PROPERTY_HIERARCHY, new GraphModelChangedEvent<>(singletonList(addEdge)))
        );
    }
}
