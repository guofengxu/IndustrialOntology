package org.industrial.ontology.domain.match;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RootCriteriaVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 11 Jun 2018
 */
public interface RootCriteriaVisitor<R> {

    @Nonnull
    R visit(@Nonnull CompositeRootCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityAnnotationCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityIsDeprecatedCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityIsNotDeprecatedCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityHasNonUniqueLangTagsCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityTypeIsOneOfCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityHasConflictingBooleanAnnotationValuesCriteria criteria);

    @Nonnull
    R visit(@Nonnull EntityAnnotationValuesAreNotDisjointCriteria criteria);

    @Nonnull
    R visit(@Nonnull IsNotBuiltInEntityCriteria criteria);

    @Nonnull
    R visit(@Nonnull SubClassOfCriteria criteria);

    @Nonnull
    R visit(@Nonnull InstanceOfCriteria instanceOfCriteria);

    @Nonnull
    R visit(@Nonnull EntityRelationshipCriteria entityRelationshipCriteria);

    @Nonnull
    R visit(EntityIsCriteria entityIsCriteria);
}
