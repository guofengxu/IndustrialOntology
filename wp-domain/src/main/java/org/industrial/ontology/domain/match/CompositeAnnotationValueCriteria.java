package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.CompositeAnnotationValueCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Jun 2018
 */
@JsonTypeName("Annotations")
public record CompositeAnnotationValueCriteria(@Nonnull @JsonProperty(CompositeAnnotationValueCriteria.ANNOTATION_VALUES) ImmutableList<? extends AnnotationValueCriteria> annotationValueCriteria, @JsonProperty(CompositeAnnotationValueCriteria.MATCH_TYPE) @Nonnull MultiMatchType multiMatchType) implements AnnotationValueCriteria {

    public CompositeAnnotationValueCriteria {
        Objects.requireNonNull(annotationValueCriteria, "Null annotationValueCriteria");
        Objects.requireNonNull(multiMatchType, "Null multiMatchType");
    }

    private static final String ANNOTATION_VALUES = "annotationValues";

    private static final String MATCH_TYPE = "matchType";

    @JsonCreator
    @Nonnull
    public static CompositeAnnotationValueCriteria get(@Nonnull @JsonProperty(ANNOTATION_VALUES) ImmutableList<? extends AnnotationValueCriteria> annotationValueCriteria, @Nonnull @JsonProperty(MATCH_TYPE) MultiMatchType multiMatchType) {
        return new CompositeAnnotationValueCriteria(annotationValueCriteria, multiMatchType);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public CompositeAnnotationValueCriteria asCompositeAnnotationValueCriteria() {
        return this;
    }

    @Nonnull
    @JsonProperty(ANNOTATION_VALUES)
    public ImmutableList<? extends AnnotationValueCriteria> getAnnotationValueCriteria() {
        return annotationValueCriteria;
    }

    @JsonProperty(MATCH_TYPE)
    @Nonnull
    public MultiMatchType getMultiMatchType() {
        return multiMatchType;
    }
}
