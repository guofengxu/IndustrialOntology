package org.industrial.ontology.kernel.event;



import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.HierarchyChangeComputer;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import javax.annotation.Nonnull;
import java.util.Arrays;

import java.util.Collection;
import java.util.Collections;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.hierarchy.HierarchyId.CLASS_HIERARCHY;
import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;

import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.AddEdge;
import org.industrial.ontology.domain.hierarchy.GraphEdge;
import org.industrial.ontology.domain.hierarchy.RemoveEdge;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.events.OWLClassHierarchyChangeComputer}.
 * <p>
* Matthew Horridge
* Stanford Center for Biomedical Informatics Research
* 22/05/15
*/
public class OWLClassHierarchyChangeComputer extends HierarchyChangeComputer<OWLClass> {

    @Nonnull
    private final EntityNodeRenderer renderer;

    @Nonnull
    private final ClassHierarchyProvider classHierarchyProvider;

    public OWLClassHierarchyChangeComputer(@Nonnull ProjectId projectId,
                                           @Nonnull ClassHierarchyProvider hierarchyProvider, @Nonnull EntityNodeRenderer renderer, @Nonnull org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider classHierarchyProvider) {
        super(projectId, EntityType.CLASS, hierarchyProvider, CLASS_HIERARCHY, renderer);
        this.renderer = checkNotNull(renderer);
        this.classHierarchyProvider = checkNotNull(classHierarchyProvider);
    }

    @Override
    protected Collection<? extends ProjectEvent> createRemovedEvents(OWLClass child, OWLClass parent) {
        RemoveEdge<EntityNode> removeEdge = new RemoveEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent)),
                new GraphNode<>(renderer.render(child))
        ));
        return Arrays.asList(
                new EntityHierarchyChangedEvent(getProjectId(), CLASS_HIERARCHY, new GraphModelChangedEvent<>(Collections.singletonList(removeEdge)))
        );
    }

    @Override
    protected Collection<? extends ProjectEvent> createAddedEvents(OWLClass child, OWLClass parent) {
        AddEdge<EntityNode> addEdge = new AddEdge<>(new GraphEdge<>(
                new GraphNode<>(renderer.render(parent), classHierarchyProvider.isLeaf(parent)),
                new GraphNode<>(renderer.render(child), classHierarchyProvider.isLeaf(child))
        ));
        return Collections.singletonList(new EntityHierarchyChangedEvent(getProjectId(),
                                                                         CLASS_HIERARCHY,
                                                                         new GraphModelChangedEvent<>(Collections.singletonList(
                                                                                 addEdge))));
    }
}
