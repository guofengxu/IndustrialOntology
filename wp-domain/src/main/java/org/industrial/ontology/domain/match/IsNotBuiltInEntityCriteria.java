package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.IsNotBuiltInEntityCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16 Jun 2018
 */
@JsonTypeName("IsNotBuiltInEntity")
public record IsNotBuiltInEntityCriteria() implements EntityMatchCriteria {

    @JsonCreator
    @Nonnull
    public static IsNotBuiltInEntityCriteria get() {
        return new IsNotBuiltInEntityCriteria();
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
