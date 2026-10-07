package org.industrial.ontology.domain.match;



import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.HierarchyPositionCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-08
 */
@JsonSubTypes({
        @JsonSubTypes.Type(SubClassOfCriteria.class),
        @JsonSubTypes.Type(InstanceOfCriteria.class),
        @JsonSubTypes.Type(CompositeHierarchyPositionCriteria.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property="match")
public interface HierarchyPositionCriteria extends Criteria {

    <R> R accept(@Nonnull HierarchyPositionCriteriaVisitor<R> visitor);
}
