package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.EntityHasNonUniqueLangTagsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10 Jun 2018
 */
@JsonTypeName("EntityHasNonUniqueLangTagsCriteria")
public record EntityHasNonUniqueLangTagsCriteria(@JsonProperty("property") @Nonnull AnnotationPropertyCriteria propertyCriteria) implements EntityMatchCriteria {

    public EntityHasNonUniqueLangTagsCriteria {
        Objects.requireNonNull(propertyCriteria, "Null propertyCriteria");
    }

    @JsonCreator
    @Nonnull
    public static EntityHasNonUniqueLangTagsCriteria get(@Nonnull @JsonProperty("property") AnnotationPropertyCriteria property) {
        return new EntityHasNonUniqueLangTagsCriteria(property);
    }

    @Override
    public <R> R accept(RootCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty("property")
    @Nonnull
    public AnnotationPropertyCriteria getPropertyCriteria() {
        return propertyCriteria;
    }
}
