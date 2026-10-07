package org.industrial.ontology.domain.frame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.match.AnyRelationshipPropertyCriteria;
import org.industrial.ontology.domain.match.AnyRelationshipValueCriteria;
import org.industrial.ontology.domain.match.RelationshipCriteria;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.RelationshipTranslationOptions}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-02
 */
public record RelationshipTranslationOptions(@JsonProperty("outgoingRelationshipCriteria") @Nullable RelationshipCriteria outgoingRelationshipCriteriaInternal, @JsonProperty("incomingRelationshipCriteria") @Nullable RelationshipCriteria incomingRelationshipCriteriaInternal, @Nonnull RelationshipMinification relationshipMinification) {

    public RelationshipTranslationOptions {
        Objects.requireNonNull(relationshipMinification, "Null relationshipMinification");
    }

    public enum RelationshipMinification {

        MINIMIZED_RELATIONSHIPS, NON_MINIMIZED_RELATIONSHIPS
    }

    @Nonnull
    public static RelationshipTranslationOptions get(@Nonnull RelationshipCriteria outgoingRelationshipCriteria, @Nullable RelationshipCriteria incomingRelationshipCriteria, @Nullable RelationshipMinification relationshipMinification) {
        return new RelationshipTranslationOptions(outgoingRelationshipCriteria, incomingRelationshipCriteria, relationshipMinification);
    }

    @Nonnull
    public static RelationshipCriteria allOutgoingRelationships() {
        return RelationshipCriteria.get(AnyRelationshipPropertyCriteria.get(), AnyRelationshipValueCriteria.get());
    }

    @Nullable
    public static RelationshipCriteria noIncomingRelationships() {
        return null;
    }

    @JsonIgnore
    public Optional<RelationshipCriteria> getOutgoingRelationshipCriteria() {
        return Optional.ofNullable(getOutgoingRelationshipCriteriaInternal());
    }

    @JsonIgnore
    public Optional<RelationshipCriteria> getIncomingRelationshipCriteria() {
        return Optional.ofNullable(getIncomingRelationshipCriteriaInternal());
    }

    @JsonProperty("outgoingRelationshipCriteria")
    @Nullable
    public RelationshipCriteria getOutgoingRelationshipCriteriaInternal() {
        return outgoingRelationshipCriteriaInternal;
    }

    @JsonProperty("incomingRelationshipCriteria")
    @Nullable
    public RelationshipCriteria getIncomingRelationshipCriteriaInternal() {
        return incomingRelationshipCriteriaInternal;
    }

    @Nonnull
    public RelationshipMinification getRelationshipMinification() {
        return relationshipMinification;
    }
}
