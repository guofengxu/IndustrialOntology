package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.StringEqualsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("StringEquals")
public record StringEqualsCriteria(String value, boolean ignoreCase) implements SimpleStringCriteria {

    public StringEqualsCriteria {
        Objects.requireNonNull(value, "Null value");
    }

    @JsonCreator
    @Nonnull
    public static StringEqualsCriteria get(@Nonnull @JsonProperty(VALUE) String value, @JsonProperty(IGNORE_CASE) boolean ignoreCase) {
        return new StringEqualsCriteria(value, ignoreCase);
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
