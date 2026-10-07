package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.CompositeHierarchyPositionCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-08
 */
@JsonTypeName("CompositeCriteria")
public record CompositeHierarchyPositionCriteria(@JsonProperty(CompositeHierarchyPositionCriteria.MATCH_TYPE) @Nonnull MultiMatchType matchType, @JsonProperty(CompositeHierarchyPositionCriteria.CRITERIA) ImmutableList<HierarchyPositionCriteria> criteria) implements HierarchyPositionCriteria {

    public CompositeHierarchyPositionCriteria {
        Objects.requireNonNull(matchType, "Null matchType");
        Objects.requireNonNull(criteria, "Null criteria");
    }

    public static final String MATCH_TYPE = "matchType";

    public static final String CRITERIA = "criteria";

    @JsonCreator
    public static CompositeHierarchyPositionCriteria get(@JsonProperty(CRITERIA) @Nonnull ImmutableList<? extends HierarchyPositionCriteria> criteria, @JsonProperty(MATCH_TYPE) @Nonnull MultiMatchType multiMatchType) {
        return new CompositeHierarchyPositionCriteria(multiMatchType, ImmutableList.copyOf(criteria));
    }

    public static CompositeHierarchyPositionCriteria get() {
        return get(ImmutableList.of(), MultiMatchType.ALL);
    }

    @Override
    public <R> R accept(@Nonnull HierarchyPositionCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(MATCH_TYPE)
    @Nonnull
    public MultiMatchType getMatchType() {
        return matchType;
    }

    @JsonProperty(CRITERIA)
    public ImmutableList<HierarchyPositionCriteria> getCriteria() {
        return criteria;
    }
}
