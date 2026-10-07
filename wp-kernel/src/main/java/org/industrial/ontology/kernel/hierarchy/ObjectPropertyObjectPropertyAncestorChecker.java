package org.industrial.ontology.kernel.hierarchy;



import org.semanticweb.owlapi.model.OWLObjectProperty;

import org.industrial.ontology.kernel.api.hierarchy.HasHasAncestor;
import org.industrial.ontology.kernel.api.hierarchy.ObjectPropertyHierarchyProvider;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ObjectPropertyObjectPropertyAncestorChecker}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 26/02/2014
 */
public class ObjectPropertyObjectPropertyAncestorChecker implements HasHasAncestor<OWLObjectProperty, OWLObjectProperty> {

    private ObjectPropertyHierarchyProvider hierarchyProvider;

    public ObjectPropertyObjectPropertyAncestorChecker(ObjectPropertyHierarchyProvider
                                                               hierarchyProvider) {
        this.hierarchyProvider = hierarchyProvider;
    }

    @Override
    public boolean hasAncestor(OWLObjectProperty node, OWLObjectProperty node2) {
        return node.equals(node2) || hierarchyProvider.getAncestors(node).contains(node2);
    }
}
