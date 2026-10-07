package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipValueEqualsLiteralCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-04
 */
@JsonTypeName("ValueEqualToLiteral")
public record RelationshipValueEqualsLiteralCriteria(@Nonnull OWLLiteral value) implements RelationshipValueEqualsCriteria {

    public RelationshipValueEqualsLiteralCriteria {
        Objects.requireNonNull(value, "Null value");
    }

    @Nonnull
    @JsonCreator
    public static RelationshipValueEqualsLiteralCriteria get(@Nonnull @JsonProperty("value") OWLLiteral literal) {
        return new RelationshipValueEqualsLiteralCriteria(literal);
    }

    @Override
    public <R> R accept(@Nonnull RelationshipValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    @Nonnull
    public OWLLiteral getValue() {
        return value;
    }
}
