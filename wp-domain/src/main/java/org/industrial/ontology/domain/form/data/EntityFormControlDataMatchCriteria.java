package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.EntityFormControlDataMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-16
 */
public record EntityFormControlDataMatchCriteria(@JsonProperty(EntityFormControlDataMatchCriteria.ENTITY_MATCHES) EntityMatchCriteria entityMatchCriteria) implements PrimitiveFormControlDataMatchCriteria {

    public EntityFormControlDataMatchCriteria {
        Objects.requireNonNull(entityMatchCriteria, "Null entityMatchCriteria");
    }

    public static final String ENTITY_MATCHES = "entityMatches";

    public static EntityFormControlDataMatchCriteria get(@Nonnull EntityMatchCriteria entityMatchCriteria) {
        return new EntityFormControlDataMatchCriteria(entityMatchCriteria);
    }

    @Override
    public <R> R accept(@Nonnull PrimitiveFormControlDataMatchCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(ENTITY_MATCHES)
    public EntityMatchCriteria getEntityMatchCriteria() {
        return entityMatchCriteria;
    }
}
