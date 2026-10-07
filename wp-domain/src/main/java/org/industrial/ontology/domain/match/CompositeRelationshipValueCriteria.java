package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.CompositeRelationshipValueCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-23
 */
@JsonTypeName("CompositeCriteria")
public record CompositeRelationshipValueCriteria(@Nonnull MultiMatchType multiMatchType, @Nonnull ImmutableList<RelationshipValueCriteria> criteria) implements RelationshipValueCriteria {

    public CompositeRelationshipValueCriteria {
        Objects.requireNonNull(multiMatchType, "Null multiMatchType");
        Objects.requireNonNull(criteria, "Null criteria");
    }

    @JsonCreator
    public static CompositeRelationshipValueCriteria get(@JsonProperty("multiMatchType") @Nonnull MultiMatchType multiMatchType, @JsonProperty("criteria") @Nonnull ImmutableList<RelationshipValueCriteria> criteria) {
        return new CompositeRelationshipValueCriteria(multiMatchType, criteria);
    }

    @Override
    public <R> R accept(@Nonnull RelationshipValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    public MultiMatchType getMultiMatchType() {
        return multiMatchType;
    }

    @Nonnull
    public ImmutableList<RelationshipValueCriteria> getCriteria() {
        return criteria;
    }
}
