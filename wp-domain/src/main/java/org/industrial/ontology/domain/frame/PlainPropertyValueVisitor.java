package org.industrial.ontology.domain.frame;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.PlainPropertyValueVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-03-31
 */
public interface PlainPropertyValueVisitor<R> {

    R visit(PlainPropertyClassValue propertyValue);

    R visit(PlainPropertyAnnotationValue propertyValue);

    R visit(PlainPropertyDatatypeValue propertyValue);

    R visit(PlainPropertyIndividualValue propertyValue);

    R visit(PlainPropertyLiteralValue propertyValue);
}
