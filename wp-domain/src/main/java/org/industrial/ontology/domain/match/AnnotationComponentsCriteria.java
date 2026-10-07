package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnnotationComponentsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 *
 * Represents criteria to match an annotation
 */
@JsonTypeName("AnnotationComponents")
public record AnnotationComponentsCriteria(@JsonProperty(AnnotationComponentsCriteria.PROPERTY) @Nonnull AnnotationPropertyCriteria annotationPropertyCriteria, @JsonProperty(AnnotationComponentsCriteria.VALUE) @Nonnull AnnotationValueCriteria annotationValueCriteria, @JsonProperty(AnnotationComponentsCriteria.ANNOTATIONS) @Nonnull AnnotationSetCriteria annotationSetCriteria) implements AnnotationCriteria {

    public AnnotationComponentsCriteria {
        Objects.requireNonNull(annotationPropertyCriteria, "Null annotationPropertyCriteria");
        Objects.requireNonNull(annotationValueCriteria, "Null annotationValueCriteria");
        Objects.requireNonNull(annotationSetCriteria, "Null annotationSetCriteria");
    }

    private static final String PROPERTY = "property";

    private static final String VALUE = "value";

    private static final String ANNOTATIONS = "annotations";

    /**
     * Creates criteria that match an annotation based on its property, its value,
     * its set of annotations.
     *  @param propertyCriteria      The criteria for matching the property.
     * @param valueCriteria         The criteria for matching the value.
     * @param annotationSetCriteria The criteria for matching annotations on the annotation.
     */
    @JsonCreator
    @Nonnull
    public static AnnotationComponentsCriteria get(@Nonnull @JsonProperty(PROPERTY) AnnotationPropertyCriteria propertyCriteria, @Nonnull @JsonProperty(VALUE) AnnotationValueCriteria valueCriteria, @Nonnull @JsonProperty(ANNOTATIONS) AnnotationSetCriteria annotationSetCriteria) {
        return new AnnotationComponentsCriteria(propertyCriteria, valueCriteria, annotationSetCriteria);
    }

    /**
     * A convenience method to create criteria that match an annotation based on its property
     * and its value.  The annotation must be present.  Annotations on the annotation are ignored.
     *
     * @param propertyCriteria The criteria for matching the property.
     * @param valueCriteria    The criteria for matching the value.
     */
    @Nonnull
    public static AnnotationComponentsCriteria get(@Nonnull @JsonProperty(PROPERTY) AnnotationPropertyCriteria propertyCriteria, @Nonnull @JsonProperty(VALUE) AnnotationValueCriteria valueCriteria) {
        return get(propertyCriteria, valueCriteria, AnyAnnotationSetCriteria.get());
    }

    /**
     * A convenicence method to create criteria that will match any annotation.
     */
    @Nonnull
    public static AnnotationCriteria anyAnnotation() {
        return get(AnyAnnotationPropertyCriteria.get(), AnyAnnotationValueCriteria.get());
    }

    @Nonnull
    @Override
    public <R> R accept(@Nonnull AnnotationCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(PROPERTY)
    @Nonnull
    public AnnotationPropertyCriteria getAnnotationPropertyCriteria() {
        return annotationPropertyCriteria;
    }

    @JsonProperty(VALUE)
    @Nonnull
    public AnnotationValueCriteria getAnnotationValueCriteria() {
        return annotationValueCriteria;
    }

    @JsonProperty(ANNOTATIONS)
    @Nonnull
    public AnnotationSetCriteria getAnnotationSetCriteria() {
        return annotationSetCriteria;
    }
}
