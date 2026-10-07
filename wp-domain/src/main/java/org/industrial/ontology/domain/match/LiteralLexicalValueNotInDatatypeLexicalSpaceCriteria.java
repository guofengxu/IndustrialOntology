package org.industrial.ontology.domain.match;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.LiteralLexicalValueNotInDatatypeLexicalSpaceCriteria}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
@JsonTypeName("LiteralLexicalValueNotInDatatypeLexicalSpace")
public record LiteralLexicalValueNotInDatatypeLexicalSpaceCriteria() implements AnnotationValueCriteria {

    @JsonCreator
    @Nonnull
    public static LiteralLexicalValueNotInDatatypeLexicalSpaceCriteria get() {
        return new LiteralLexicalValueNotInDatatypeLexicalSpaceCriteria();
    }

    @Override
    public <R> R accept(@Nonnull AnnotationValueCriteriaVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
