package org.industrial.ontology.domain.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.LangTagCriteriaVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
public interface LangTagCriteriaVisitor<R> {

    R visit(@Nonnull LangTagMatchesCriteria criteria);

    R visit(@Nonnull LangTagIsEmptyCriteria criteria);

    R visit(@Nonnull AnyLangTagOrEmptyLangTagCriteria criteria);
}
