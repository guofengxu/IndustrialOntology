package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.LangTagMatchesCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("LangTagMatches")
public record LangTagMatchesCriteria(@JsonProperty(LangTagMatchesCriteria.RANGE) String languageRange) implements LangTagCriteria {

    public LangTagMatchesCriteria {
        Objects.requireNonNull(languageRange, "Null languageRange");
    }

    private static final String RANGE = "languageRange";

    @JsonCreator
    @Nonnull
    public static LangTagMatchesCriteria get(@Nonnull @JsonProperty(RANGE) String languageRange) {
        return new LangTagMatchesCriteria(languageRange);
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(@Nonnull LiteralCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(RANGE)
    public String getLanguageRange() {
        return languageRange;
    }
}
