package org.industrial.ontology.kernel.hierarchy;



import org.semanticweb.owlapi.model.OWLDataProperty;

import org.industrial.ontology.kernel.api.hierarchy.HasHasAncestor;
import org.industrial.ontology.kernel.api.hierarchy.HierarchyProvider;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.DataPropertyDataPropertyAncestorChecker}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 26/02/2014
 */
public class DataPropertyDataPropertyAncestorChecker implements HasHasAncestor<OWLDataProperty, OWLDataProperty> {

    private HierarchyProvider<OWLDataProperty> hierarchyProvider;

    public DataPropertyDataPropertyAncestorChecker(HierarchyProvider<OWLDataProperty>
                                                           hierarchyProvider) {
        this.hierarchyProvider = hierarchyProvider;
    }

    @Override
    public boolean hasAncestor(OWLDataProperty node, OWLDataProperty node2) {
        return node.equals(node2) || hierarchyProvider.getAncestors(node).contains(node2);
    }
}
