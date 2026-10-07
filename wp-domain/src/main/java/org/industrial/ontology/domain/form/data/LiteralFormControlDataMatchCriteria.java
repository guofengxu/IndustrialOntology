package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.match.LiteralCriteria;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.LiteralFormControlDataMatchCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-16
 */
public record LiteralFormControlDataMatchCriteria(@JsonProperty(LiteralFormControlDataMatchCriteria.LITERAL_MATCHES) @Nonnull LiteralCriteria lexicalValueCriteria) implements PrimitiveFormControlDataMatchCriteria {

    public LiteralFormControlDataMatchCriteria {
        Objects.requireNonNull(lexicalValueCriteria, "Null lexicalValueCriteria");
    }

    public static final String LITERAL_MATCHES = "literalMatches";

    @Nonnull
    public static LiteralFormControlDataMatchCriteria get(@Nonnull LiteralCriteria literalCriteria) {
        return new LiteralFormControlDataMatchCriteria(literalCriteria);
    }

    @Override
    public <R> R accept(@Nonnull PrimitiveFormControlDataMatchCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(LITERAL_MATCHES)
    @Nonnull
    public LiteralCriteria getLexicalValueCriteria() {
        return lexicalValueCriteria;
    }
}
