package org.industrial.ontology.domain.match;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.NumericValueCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("NumericValue")
@JsonPropertyOrder({ NumericValueCriteria.PREDICATE, NumericValueCriteria.VALUE })
public record NumericValueCriteria(@JsonIgnore String name, double value) implements LexicalValueCriteria {

    public NumericValueCriteria {
        Objects.requireNonNull(name, "Null name");
    }

    public static final String PREDICATE = "predicate";

    public static final String VALUE = "value";

    @Nonnull
    @JsonProperty(PREDICATE)
    public NumericPredicate getPredicate() {
        return NumericPredicate.valueOf(getName());
    }

    @Nonnull
    public static NumericValueCriteria get(@Nonnull String name, @JsonProperty(VALUE) double value) {
        return new NumericValueCriteria(name, value);
    }

    @JsonCreator
    @Nonnull
    public static NumericValueCriteria get(@Nonnull @JsonProperty(PREDICATE) NumericPredicate predicate, @JsonProperty(VALUE) double value) {
        return new NumericValueCriteria(predicate.name(), value);
    }

    @Nonnull
    public static NumericValueCriteria numericValue(@Nonnull NumericPredicate predicate, double value) {
        return get(predicate, value);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull LiteralCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }
}
