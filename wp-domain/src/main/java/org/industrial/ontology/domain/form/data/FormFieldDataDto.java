package org.industrial.ontology.domain.form.data;

import org.industrial.ontology.domain.form.field.FormFieldDescriptorDto;
import org.industrial.ontology.domain.pagination.Page;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormFieldDataDto}.
 */
public record FormFieldDataDto(@Nonnull FormFieldDescriptorDto formFieldDescriptor, @Nonnull Page<FormControlDataDto> formControlData) {

    public FormFieldDataDto {
        Objects.requireNonNull(formFieldDescriptor, "Null formFieldDescriptor");
        Objects.requireNonNull(formControlData, "Null formControlData");
    }

    @Nonnull
    public static FormFieldDataDto get(@Nonnull FormFieldDescriptorDto descriptor, @Nonnull Page<FormControlDataDto> formControlData) {
        return new FormFieldDataDto(descriptor, formControlData);
    }

    @Nonnull
    public FormFieldData toFormFieldData() {
        return FormFieldData.get(getFormFieldDescriptor().toFormFieldDescriptor(), getFormControlData().transform(FormControlDataDto::toFormControlData));
    }

    @Nonnull
    public FormFieldData getFormFieldData() {
        return FormFieldData.get(getFormFieldDescriptor().toFormFieldDescriptor(), getFormControlData().transform(FormControlDataDto::toFormControlData));
    }

    @Nonnull
    public FormFieldDescriptorDto getFormFieldDescriptor() {
        return formFieldDescriptor;
    }

    /**
     * Gets the page of form control values for this field.
     */
    @Nonnull
    public Page<FormControlDataDto> getFormControlData() {
        return formControlData;
    }
}
