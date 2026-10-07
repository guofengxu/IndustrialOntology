package org.industrial.ontology.domain.match;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.criteria.RelationshipValueCriteriaVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
public interface RelationshipValueCriteriaVisitor<R> {

    R visit(AnyRelationshipValueCriteria criteria);

    R visit(RelationshipValueMatchesCriteria criteria);

    R visit(RelationshipValueEqualsEntityCriteria criteria);

    R visit(RelationshipValueEqualsLiteralCriteria criteria);

    R visit(CompositeRelationshipValueCriteria compositeRelationshipValueCriteria);
}
