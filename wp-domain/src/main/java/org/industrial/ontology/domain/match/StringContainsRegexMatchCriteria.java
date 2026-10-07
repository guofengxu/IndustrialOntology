package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.StringContainsRegexMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@JsonTypeName("StringContainsRegexMatch")
public record StringContainsRegexMatchCriteria(@JsonProperty(PATTERN) @Nonnull String pattern, @JsonProperty(IGNORE_CASE) boolean ignoreCase) implements RegexMatchCriteria {

    public StringContainsRegexMatchCriteria {
        Objects.requireNonNull(pattern, "Null pattern");
    }

    @JsonCreator
    @Nonnull
    public static StringContainsRegexMatchCriteria get(@Nonnull @JsonProperty(PATTERN) String pattern, @JsonProperty(IGNORE_CASE) boolean ignoreCase) {
        return new StringContainsRegexMatchCriteria(pattern, ignoreCase);
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
    @JsonProperty(PATTERN)
    @Nonnull
    public String getPattern() {
        return pattern;
    }

    @Override
    @JsonProperty(IGNORE_CASE)
    public boolean isIgnoreCase() {
        return ignoreCase;
    }
}
