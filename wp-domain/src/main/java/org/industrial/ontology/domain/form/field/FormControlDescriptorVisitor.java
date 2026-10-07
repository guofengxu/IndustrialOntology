package org.industrial.ontology.domain.form.field;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormControlDescriptorVisitor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public interface FormControlDescriptorVisitor<R> {

    R visit(TextControlDescriptor textControlDescriptor);

    R visit(NumberControlDescriptor numberControlDescriptor);

    R visit(SingleChoiceControlDescriptor singleChoiceControlDescriptor);

    R visit(MultiChoiceControlDescriptor multiChoiceControlDescriptor);

    R visit(EntityNameControlDescriptor entityNameControlDescriptor);

    R visit(ImageControlDescriptor imageControlDescriptor);

    R visit(GridControlDescriptor gridControlDescriptor);

    R visit(SubFormControlDescriptor subFormControlDescriptor);
}
