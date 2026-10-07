package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityRelationshipCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeName("EntityRelationship")
public record EntityRelationshipCriteria(@Nonnull @JsonProperty(EntityRelationshipCriteria.PRESENCE) RelationshipPresence relationshipPresence, @Nonnull @JsonProperty(EntityRelationshipCriteria.PROPERTY) RelationshipPropertyCriteria relationshipPropertyCriteria, @Nonnull @JsonProperty(EntityRelationshipCriteria.VALUE) RelationshipValueCriteria relationshipValueCriteria) implements EntityMatchCriteria {

    public EntityRelationshipCriteria {
        Objects.requireNonNull(relationshipPresence, "Null relationshipPresence");
        Objects.requireNonNull(relationshipPropertyCriteria, "Null relationshipPropertyCriteria");
        Objects.requireNonNull(relationshipValueCriteria, "Null relationshipValueCriteria");
    }

    private static final String PRESENCE = "presence";

    private static final String PROPERTY = "property";

    private static final String VALUE = "value";

    @Nonnull
    @JsonCreator
    public static EntityRelationshipCriteria get(@Nonnull @JsonProperty(PRESENCE) RelationshipPresence presence, @Nonnull @JsonProperty(PROPERTY) RelationshipPropertyCriteria propertyCriteria, @Nonnull @JsonProperty(VALUE) RelationshipValueCriteria valueCriteria) {
        return new EntityRelationshipCriteria(presence, propertyCriteria, valueCriteria);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @JsonProperty(PRESENCE)
    public RelationshipPresence getRelationshipPresence() {
        return relationshipPresence;
    }

    @Nonnull
    @JsonProperty(PROPERTY)
    public RelationshipPropertyCriteria getRelationshipPropertyCriteria() {
        return relationshipPropertyCriteria;
    }

    @Nonnull
    @JsonProperty(VALUE)
    public RelationshipValueCriteria getRelationshipValueCriteria() {
        return relationshipValueCriteria;
    }
}
