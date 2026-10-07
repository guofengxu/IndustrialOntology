package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.StringStartsWithCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("StringStartsWith")
public record StringStartsWithCriteria(String value, boolean ignoreCase) implements SimpleStringCriteria {

    public StringStartsWithCriteria {
        Objects.requireNonNull(value, "Null value");
    }

    @JsonCreator
    @Nonnull
    public static StringStartsWithCriteria get(@Nonnull @JsonProperty(VALUE) String value, @JsonProperty(IGNORE_CASE) boolean ignoreCase) {
        return new StringStartsWithCriteria(value, ignoreCase);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull LiteralCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
