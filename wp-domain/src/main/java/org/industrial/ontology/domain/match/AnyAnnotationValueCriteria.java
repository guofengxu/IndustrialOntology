package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnyAnnotationValueCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 *
 * Criteria that matches any annotation value
 */
@JsonTypeName("AnyAnnotationValue")
public record AnyAnnotationValueCriteria() implements AnnotationValueCriteria {

    @Nonnull
    @JsonCreator
    public static AnyAnnotationValueCriteria get() {
        return new AnyAnnotationValueCriteria();
    }

    /**
     * A convenicen method that returns an instance of {@link AnyAnnotationPropertyCriteria}.
     */
    @Nonnull
    public static AnyAnnotationValueCriteria anyValue() {
        return get();
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
