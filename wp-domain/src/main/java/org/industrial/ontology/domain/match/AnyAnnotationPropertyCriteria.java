package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyAnnotationPropertyCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 *
 * Criteria for matching any annotation property
 */
@JsonTypeName("AnyAnnotationProperty")
public record AnyAnnotationPropertyCriteria() implements AnnotationPropertyCriteria {

    @Nonnull
    @JsonCreator
    public static AnyAnnotationPropertyCriteria get() {
        return new AnyAnnotationPropertyCriteria();
    }

    @Nonnull
    public static AnyAnnotationPropertyCriteria anyAnnotationProperty() {
        return get();
    }

    @Override
    public <R> R accept(@Nonnull AnnotationPropertyCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
