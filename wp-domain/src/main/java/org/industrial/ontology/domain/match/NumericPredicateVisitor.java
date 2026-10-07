package org.industrial.ontology.domain.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.NumericPredicateVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10 Jun 2018
 */
public interface NumericPredicateVisitor<A, R> {

    R isGreaterThan(@Nonnull A arg);

    R isGreaterThanOrEqualTo(@Nonnull A arg);

    R isLessThan(@Nonnull A arg);

    R isLessThanOrEqualTo(@Nonnull A arg);

    R isEqualTo(@Nonnull A arg);
}
