package org.industrial.ontology.kernel.api.match;



import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.match.RelationshipCriteria;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.RelationshipMatcherFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public interface RelationshipMatcherFactory {

    Matcher<PlainPropertyValue> getRelationshipMatcher(@Nonnull RelationshipCriteria relationshipCriteria);
}
