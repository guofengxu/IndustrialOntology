package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import org.semanticweb.owlapi.model.EntityType;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityTypeIsOneOfCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15 Jun 2018
 */
@JsonTypeName("EntityTypeIsOneOf")
public record EntityTypeIsOneOfCriteria(@JsonProperty(EntityTypeIsOneOfCriteria.TYPES) @Nonnull ImmutableSet<EntityType<?>> entityTypes) implements EntityMatchCriteria {

    public EntityTypeIsOneOfCriteria {
        Objects.requireNonNull(entityTypes, "Null entityTypes");
    }

    private static final String TYPES = "types";

    @JsonCreator
    @Nonnull
    public static EntityTypeIsOneOfCriteria get(@Nonnull @JsonProperty(TYPES) ImmutableSet<EntityType<?>> entityTypes) {
        return new EntityTypeIsOneOfCriteria(entityTypes);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(TYPES)
    @Nonnull
    public ImmutableSet<EntityType<?>> getEntityTypes() {
        return entityTypes;
    }
}
