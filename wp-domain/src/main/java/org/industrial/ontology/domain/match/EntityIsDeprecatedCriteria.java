package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityIsDeprecatedCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10 Jun 2018
 */
@JsonTypeName("EntityIsDeprecated")
public record EntityIsDeprecatedCriteria() implements EntityMatchCriteria {

    @JsonCreator
    @Nonnull
    public static EntityIsDeprecatedCriteria get() {
        return new EntityIsDeprecatedCriteria();
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
