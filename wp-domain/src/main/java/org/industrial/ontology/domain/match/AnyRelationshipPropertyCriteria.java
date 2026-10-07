package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyRelationshipPropertyCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeName("AnyProperty")
public record AnyRelationshipPropertyCriteria() implements RelationshipPropertyCriteria {

    @Nonnull
    @JsonCreator
    public static AnyRelationshipPropertyCriteria get() {
        return new AnyRelationshipPropertyCriteria();
    }

    @Override
    public <R> R accept(@Nonnull RelationshipPropertyCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
