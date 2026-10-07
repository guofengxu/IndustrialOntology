package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.CompositeRootCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Jun 2018
 */
@JsonTypeName("CompositeCriteria")
public record CompositeRootCriteria(@JsonProperty(CompositeRootCriteria.CRITERIA) @Nonnull ImmutableList<? extends RootCriteria> rootCriteria, @JsonIgnore int multiMatchTypeOrdinal) implements RootCriteria, EntityMatchCriteria {

    public CompositeRootCriteria {
        Objects.requireNonNull(rootCriteria, "Null rootCriteria");
    }

    private static final String MATCH_TYPE = "matchType";

    private static final String CRITERIA = "criteria";

    @JsonProperty(MATCH_TYPE)
    @Nonnull
    public MultiMatchType getMatchType() {
        return MultiMatchType.values()[getMultiMatchTypeOrdinal()];
    }

    @JsonCreator
    public static CompositeRootCriteria get(@Nonnull @JsonProperty(CRITERIA) ImmutableList<? extends RootCriteria> rootCriteria, @Nonnull @JsonProperty(MATCH_TYPE) MultiMatchType matchType) {
        return new CompositeRootCriteria(rootCriteria, matchType.ordinal());
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public CompositeRootCriteria asCompositeRootCriteria() {
        return this;
    }

    @JsonProperty(CRITERIA)
    @Nonnull
    public ImmutableList<? extends RootCriteria> getRootCriteria() {
        return rootCriteria;
    }

    @JsonIgnore
    public int getMultiMatchTypeOrdinal() {
        return multiMatchTypeOrdinal;
    }
}
