package org.industrial.ontology.kernel.hierarchy;



import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.kernel.event.EventTranslator;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.event.ProjectEvent;
import org.industrial.ontology.domain.event.EntityHierarchyChangedEvent;
import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.industrial.ontology.kernel.api.hierarchy.HierarchyProvider;

import org.industrial.ontology.domain.hierarchy.GraphModelChangedEvent;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.GraphModelChange;
import org.industrial.ontology.domain.hierarchy.AddRootNode;
import org.industrial.ontology.domain.hierarchy.RemoveRootNode;
import java.util.List;
import java.util.Collections;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.HierarchyChangeComputer}.
 * <p>
 * Author: Matthew Horridge<br> Stanford University<br> Bio-Medical Informatics Research Group<br> Date: 21/03/2013
 */
public abstract class HierarchyChangeComputer<T extends OWLEntity> implements EventTranslator {

    private final ProjectId projectId;

    private final HierarchyProvider<T> hierarchyProvider;

    private final EntityType<T> entityType;

    private final HierarchyId hierarchyId;

    private final EntityNodeRenderer renderer;


    private SetMultimap<T, T> child2ParentMap = HashMultimap.create();

    private Set<T> roots = new HashSet<>();

    public HierarchyChangeComputer(ProjectId projectId, EntityType<T> entityType, HierarchyProvider<T> hierarchyProvider, HierarchyId hierarchyId, EntityNodeRenderer renderer) {
        this.projectId = projectId;
        this.hierarchyProvider = hierarchyProvider;
        this.entityType = entityType;
        this.hierarchyId = hierarchyId;
        this.renderer = renderer;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void prepareForOntologyChanges(List<OntologyChange> submittedChanges) {
        for (OntologyChange change : submittedChanges) {
            for (OWLEntity entity : change.getSignature()) {
                if (entity.isType(entityType)) {
                    final T t = (T) entity;
                    final Collection<T> parentsBefore = hierarchyProvider.getParents(t);
                    child2ParentMap.putAll(t, parentsBefore);
                }
            }
        }
        roots.addAll(hierarchyProvider.getRoots());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void translateOntologyChanges(Revision revision, ChangeApplicationResult<?> result, List<ProjectEvent> projectEventList) {
        Set<T> changeSignature = new HashSet<>();
        for (OntologyChange change : result.getChangeList()) {
            for (OWLEntity child : change.getSignature()) {
                if (child.isType(entityType)) {
                    final T t = (T) child;
                    if (!changeSignature.contains(t)) {
                        changeSignature.add(t);
                        Set<T> parentsBefore = child2ParentMap.get(t);
                        Collection<T> parentsAfter = hierarchyProvider.getParents(t);
                        for (T parentBefore : parentsBefore) {
                            if (!parentsAfter.contains(parentBefore)) {
                                // Removed
                                projectEventList.addAll(createRemovedEvents(t, parentBefore));

                            }
                        }
                        for (T parentAfter : parentsAfter) {
                            if (!parentsBefore.contains(parentAfter)) {
                                // Added
                                projectEventList.addAll(createAddedEvents(t, parentAfter));
                            }
                        }
                    }
                }
            }
        }
        Set<T> rootsAfter = new HashSet<>(hierarchyProvider.getRoots());
        for (T rootAfter : rootsAfter) {
            if (!roots.contains(rootAfter)) {
                List<GraphModelChange<EntityNode>> changes = Collections.singletonList(new AddRootNode<>(
                        new GraphNode<>(renderer.render(rootAfter),
                                        hierarchyProvider.isLeaf(rootAfter))));
                EntityHierarchyChangedEvent event = new EntityHierarchyChangedEvent(projectId,
                                                                                    hierarchyId,
                                                                                    new GraphModelChangedEvent<>(changes));
                projectEventList.add(event);
            }
        }
        for (T rootBefore : roots) {
            if (!rootsAfter.contains(rootBefore)) {
                List<GraphModelChange<EntityNode>> changes = Collections.singletonList(new RemoveRootNode<>(
                        new GraphNode<>(renderer.render(rootBefore))));
                EntityHierarchyChangedEvent event = new EntityHierarchyChangedEvent(projectId,
                                                                                    hierarchyId,
                                                                                    new GraphModelChangedEvent<>(changes));
                projectEventList.add(event);
            }
        }
    }


    protected abstract Collection<? extends ProjectEvent> createRemovedEvents(T child, T parent);

    protected abstract Collection<? extends ProjectEvent> createAddedEvents(T child, T parent);

}
