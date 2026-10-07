package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityIsNotDeprecatedCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10 Jun 2018
 */
@JsonTypeName("EntityIsNotDeprecated")
public record EntityIsNotDeprecatedCriteria() implements EntityMatchCriteria {

    @JsonCreator
    @Nonnull
    public static EntityIsNotDeprecatedCriteria get() {
        return new EntityIsNotDeprecatedCriteria();
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
