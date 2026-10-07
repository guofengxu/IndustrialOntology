package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.IriHasAnnotationCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@JsonTypeName("IriHasAnnotation")
public record IriHasAnnotationCriteria(@JsonProperty(IriHasAnnotationCriteria.ANNOTATION_CRITERIA) @Nonnull AnnotationCriteria iriAnnotationCriteria) implements IriCriteria {

    public IriHasAnnotationCriteria {
        Objects.requireNonNull(iriAnnotationCriteria, "Null iriAnnotationCriteria");
    }

    private static final String ANNOTATION_CRITERIA = "annotationCriteria";

    @JsonCreator
    @Nonnull
    public static IriHasAnnotationCriteria get(@Nonnull @JsonProperty(ANNOTATION_CRITERIA) AnnotationCriteria annotationCriteria) {
        return new IriHasAnnotationCriteria(annotationCriteria);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(ANNOTATION_CRITERIA)
    @Nonnull
    public AnnotationCriteria getIriAnnotationCriteria() {
        return iriAnnotationCriteria;
    }
}
