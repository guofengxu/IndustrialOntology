package org.industrial.ontology.domain.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnnotationPropertyCriteriaVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
public interface AnnotationPropertyCriteriaVisitor<R> {

    R visit(@Nonnull AnyAnnotationPropertyCriteria criteria);

    R visit(@Nonnull IriEqualsCriteria criteria);
}
