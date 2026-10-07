package org.industrial.ontology.kernel.api.hierarchy;



import java.util.Collection;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.hierarchy.HasGetAncestors}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 Apr 2017
 */
public interface HasGetAncestors<N> {

    Collection<N> getAncestors(N object);
}
