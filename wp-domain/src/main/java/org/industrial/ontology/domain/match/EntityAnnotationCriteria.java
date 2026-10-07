package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityAnnotationCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("EntityAnnotation")
public record EntityAnnotationCriteria(@JsonProperty(EntityAnnotationCriteria.ANNOTATION) @Nonnull AnnotationCriteria annotationCriteria, @JsonIgnore int annotationPresenceOrdinal) implements EntityMatchCriteria {

    public EntityAnnotationCriteria {
        Objects.requireNonNull(annotationCriteria, "Null annotationCriteria");
    }

    private static final String ANNOTATION = "annotation";

    private static final String PRESENCE = "presence";

    @JsonProperty(PRESENCE)
    @Nonnull
    public AnnotationPresence getAnnotationPresence() {
        return AnnotationPresence.values()[getAnnotationPresenceOrdinal()];
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonCreator
    @Nonnull
    public static EntityAnnotationCriteria get(@Nonnull @JsonProperty(ANNOTATION) AnnotationCriteria criteria, @Nonnull @JsonProperty(PRESENCE) AnnotationPresence presence) {
        return new EntityAnnotationCriteria(criteria, presence.ordinal());
    }

    @Nonnull
    public static EntityAnnotationCriteria get(@Nonnull @JsonProperty("annotation") AnnotationCriteria criteria) {
        return new EntityAnnotationCriteria(criteria, AnnotationPresence.AT_LEAST_ONE.ordinal());
    }

    public static EntityAnnotationCriteria get(@Nonnull AnnotationPropertyCriteria propertyCriteria, @Nonnull AnnotationValueCriteria valueCriteria) {
        return get(AnnotationComponentsCriteria.get(propertyCriteria, valueCriteria));
    }

    @JsonProperty(ANNOTATION)
    @Nonnull
    public AnnotationCriteria getAnnotationCriteria() {
        return annotationCriteria;
    }

    @JsonIgnore
    public int getAnnotationPresenceOrdinal() {
        return annotationPresenceOrdinal;
    }
}
