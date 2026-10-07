package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.StringContainsCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("StringContains")
public record StringContainsCriteria(String value, boolean ignoreCase) implements SimpleStringCriteria {

    public StringContainsCriteria {
        Objects.requireNonNull(value, "Null value");
    }

    @JsonCreator
    @Nonnull
    public static StringContainsCriteria get(@Nullable @JsonProperty(VALUE) String value, @JsonProperty(IGNORE_CASE) boolean ignoreCase) {
        return new StringContainsCriteria(value == null ? "" : value, ignoreCase);
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
