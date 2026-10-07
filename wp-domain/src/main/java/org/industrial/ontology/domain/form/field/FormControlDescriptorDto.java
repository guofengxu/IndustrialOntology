package org.industrial.ontology.domain.form.field;




/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormControlDescriptorDto}.
 */
public interface FormControlDescriptorDto {

    <R> R accept(FormControlDescriptorDtoVisitor<R> visitor);

    FormControlDescriptor toFormControlDescriptor();
}
