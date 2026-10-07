package org.industrial.ontology.kernel.api.match;



import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityMatcherFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-15
 */
public interface EntityMatcherFactory {

    @Nonnull
    Matcher<OWLEntity> getEntityMatcher(@Nonnull EntityMatchCriteria criteria);
}
