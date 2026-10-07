package org.industrial.ontology.domain.form.data;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.match.MultiMatchType;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.CompositePrimitiveFormControlDataMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-16
 */
public record CompositePrimitiveFormControlDataMatchCriteria(@Nonnull MultiMatchType multiMatchType, @Nonnull ImmutableList<PrimitiveFormControlDataMatchCriteria> criteria) implements PrimitiveFormControlDataMatchCriteria {

    public CompositePrimitiveFormControlDataMatchCriteria {
        Objects.requireNonNull(multiMatchType, "Null multiMatchType");
        Objects.requireNonNull(criteria, "Null criteria");
    }

    @Nonnull
    public static CompositePrimitiveFormControlDataMatchCriteria get(@Nonnull ImmutableList<? extends PrimitiveFormControlDataMatchCriteria> criteria, @Nonnull MultiMatchType matchType) {
        return new CompositePrimitiveFormControlDataMatchCriteria(matchType, ImmutableList.copyOf(criteria));
    }

    @Override
    public <R> R accept(@Nonnull PrimitiveFormControlDataMatchCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    public MultiMatchType getMultiMatchType() {
        return multiMatchType;
    }

    @Nonnull
    public ImmutableList<PrimitiveFormControlDataMatchCriteria> getCriteria() {
        return criteria;
    }
}
