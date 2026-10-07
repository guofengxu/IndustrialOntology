package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "match")
@JsonSubTypes({ @Type(AnnotationComponentsCriteria.class) })
public record RelationshipCriteria(@JsonProperty("property") @Nonnull RelationshipPropertyCriteria propertyCriteria, @JsonProperty("value") @Nonnull RelationshipValueCriteria valueCriteria) implements Criteria {

    public RelationshipCriteria {
        Objects.requireNonNull(propertyCriteria, "Null propertyCriteria");
        Objects.requireNonNull(valueCriteria, "Null valueCriteria");
    }

    @Nonnull
    public static RelationshipCriteria get(@Nonnull RelationshipPropertyCriteria propertyCriteria, @Nonnull RelationshipValueCriteria valueCriteria) {
        return new RelationshipCriteria(propertyCriteria, valueCriteria);
    }

    @JsonProperty("property")
    @Nonnull
    public RelationshipPropertyCriteria getPropertyCriteria() {
        return propertyCriteria;
    }

    @JsonProperty("value")
    @Nonnull
    public RelationshipValueCriteria getValueCriteria() {
        return valueCriteria;
    }
}
