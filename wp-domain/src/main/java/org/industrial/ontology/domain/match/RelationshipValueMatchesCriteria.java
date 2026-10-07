package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipValueMatchesCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeName("ValueMatches")
public record RelationshipValueMatchesCriteria(@JsonProperty("criteria") @Nonnull CompositeRootCriteria matchCriteria) implements RelationshipValueCriteria {

    public RelationshipValueMatchesCriteria {
        Objects.requireNonNull(matchCriteria, "Null matchCriteria");
    }

    @Nonnull
    public static RelationshipValueMatchesCriteria get(@Nonnull @JsonProperty("criteria") CompositeRootCriteria criteria) {
        return new RelationshipValueMatchesCriteria(criteria);
    }

    /**
     * Support earlier serializations that just used to use an {@link EntityMatchCriteria}.
     */
    @Nonnull
    @JsonCreator
    protected static RelationshipValueMatchesCriteria get(@Nullable @JsonProperty("criteria") CompositeRootCriteria criteria, @Nullable @JsonProperty("matchCriteria") EntityMatchCriteria entityMatchCriteria) {
        if (entityMatchCriteria != null) {
            return new RelationshipValueMatchesCriteria(CompositeRootCriteria.get(ImmutableList.of(entityMatchCriteria), MultiMatchType.ALL));
        } else {
            return new RelationshipValueMatchesCriteria(criteria);
        }
    }

    @Override
    public <R> R accept(@Nonnull RelationshipValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty("criteria")
    @Nonnull
    public CompositeRootCriteria getMatchCriteria() {
        return matchCriteria;
    }
}
