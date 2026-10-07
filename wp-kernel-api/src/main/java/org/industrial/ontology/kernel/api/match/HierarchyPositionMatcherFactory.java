package org.industrial.ontology.kernel.api.match;



import org.industrial.ontology.domain.match.CompositeHierarchyPositionCriteria;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.HierarchyPositionMatcherFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-08
 */
public interface HierarchyPositionMatcherFactory {

    @Nonnull
    Matcher<OWLEntity> getHierarchyPositionMatcher(@Nonnull CompositeHierarchyPositionCriteria criteria);
}
