package org.industrial.ontology.domain.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.AnnotationCriteriaVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 17 Jun 2018
 */
public interface AnnotationCriteriaVisitor<R> {

    @Nonnull
    R visit(@Nonnull AnnotationComponentsCriteria criteria);
}
