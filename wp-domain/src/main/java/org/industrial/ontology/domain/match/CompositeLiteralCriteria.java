package org.industrial.ontology.domain.match;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.CompositeLiteralCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-18
 */
public record CompositeLiteralCriteria(@Nonnull ImmutableList<LiteralCriteria> criteria, @Nonnull MultiMatchType multiMatchType) implements LiteralCriteria {

    public CompositeLiteralCriteria {
        Objects.requireNonNull(criteria, "Null criteria");
        Objects.requireNonNull(multiMatchType, "Null multiMatchType");
    }

    public static CompositeLiteralCriteria get(@Nonnull ImmutableList<? extends LiteralCriteria> criteria, @Nonnull MultiMatchType matchType) {
        return new CompositeLiteralCriteria(ImmutableList.copyOf(criteria), matchType);
    }

    @Override
    public <R> R accept(@Nonnull LiteralCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    public ImmutableList<LiteralCriteria> getCriteria() {
        return criteria;
    }

    @Nonnull
    public MultiMatchType getMultiMatchType() {
        return multiMatchType;
    }
}
