package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityIsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-09
 */
@JsonTypeName("EntityIs")
public record EntityIsCriteria(@Nonnull OWLEntity entity) implements EntityMatchCriteria {

    public EntityIsCriteria {
        Objects.requireNonNull(entity, "Null entity");
    }

    @JsonCreator
    public static EntityIsCriteria get(@Nonnull @JsonProperty("entity") OWLEntity entity) {
        return new EntityIsCriteria(entity);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public CompositeRootCriteria asCompositeRootCriteria() {
        return CompositeRootCriteria.get(ImmutableList.of(this), MultiMatchType.ALL);
    }

    @Nonnull
    public OWLEntity getEntity() {
        return entity;
    }
}
