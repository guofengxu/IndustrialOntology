package org.industrial.ontology.kernel.api.hierarchy;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.HasHasAncestor}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 26/02/2014
 */
public interface HasHasAncestor<N, M> {

    boolean hasAncestor(N node, M node2);
}
