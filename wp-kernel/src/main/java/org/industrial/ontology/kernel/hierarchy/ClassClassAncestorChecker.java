package org.industrial.ontology.kernel.hierarchy;



import org.semanticweb.owlapi.model.OWLClass;

import org.industrial.ontology.kernel.api.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.api.hierarchy.HasHasAncestor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.ClassClassAncestorChecker}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 26/02/2014
 */
public class ClassClassAncestorChecker implements HasHasAncestor<OWLClass, OWLClass> {

    private ClassHierarchyProvider hierarchyProvider;

    public ClassClassAncestorChecker(ClassHierarchyProvider hierarchyProvider) {
        this.hierarchyProvider = hierarchyProvider;
    }

    @Override
    public boolean hasAncestor(OWLClass node, OWLClass node2) {
        return node.equals(node2) || hierarchyProvider.isAncestor(node, node2);
    }
}
