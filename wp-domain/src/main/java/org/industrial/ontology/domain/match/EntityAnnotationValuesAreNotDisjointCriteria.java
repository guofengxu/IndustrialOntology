package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityAnnotationValuesAreNotDisjointCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@JsonTypeName("EntityAnnotationValuesAreNotDisjoint")
public record EntityAnnotationValuesAreNotDisjointCriteria(@JsonProperty("firstProperty") @Nonnull AnnotationPropertyCriteria firstProperty, @JsonProperty("secondProperty") @Nonnull AnnotationPropertyCriteria secondProperty) implements EntityMatchCriteria {

    public EntityAnnotationValuesAreNotDisjointCriteria {
        Objects.requireNonNull(firstProperty, "Null firstProperty");
        Objects.requireNonNull(secondProperty, "Null secondProperty");
    }

    @JsonCreator
    @Nonnull
    public static EntityAnnotationValuesAreNotDisjointCriteria get(@Nonnull @JsonProperty("firstProperty") AnnotationPropertyCriteria firstProperty, @Nonnull @JsonProperty("secondProperty") AnnotationPropertyCriteria secondProperty) {
        return new EntityAnnotationValuesAreNotDisjointCriteria(firstProperty, secondProperty);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty("firstProperty")
    @Nonnull
    public AnnotationPropertyCriteria getFirstProperty() {
        return firstProperty;
    }

    @JsonProperty("secondProperty")
    @Nonnull
    public AnnotationPropertyCriteria getSecondProperty() {
        return secondProperty;
    }
}
