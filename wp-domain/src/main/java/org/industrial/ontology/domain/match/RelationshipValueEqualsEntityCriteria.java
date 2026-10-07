package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipValueEqualsEntityCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
@JsonTypeName("ValueEqualToEntity")
public record RelationshipValueEqualsEntityCriteria(@Nonnull @JsonProperty(RelationshipValueEqualsEntityCriteria.VALUE) OWLEntity value) implements RelationshipValueEqualsCriteria {

    public RelationshipValueEqualsEntityCriteria {
        Objects.requireNonNull(value, "Null value");
    }

    private static final String VALUE = "value";

    @JsonCreator
    public static RelationshipValueEqualsEntityCriteria get(@Nonnull @JsonProperty(VALUE) OWLEntity value) {
        return new RelationshipValueEqualsEntityCriteria(value);
    }

    @Override
    public <R> R accept(@Nonnull RelationshipValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @JsonProperty(VALUE)
    public OWLEntity getValue() {
        return value;
    }
}
