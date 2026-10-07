package org.industrial.ontology.domain.form.data;

import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.pagination.Page;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormFieldData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-06
 */
public record FormFieldData(@Nonnull FormFieldDescriptor formFieldDescriptor, @Nonnull Page<FormControlData> formControlData) {

    public FormFieldData {
        Objects.requireNonNull(formFieldDescriptor, "Null formFieldDescriptor");
        Objects.requireNonNull(formControlData, "Null formControlData");
    }

    public static FormFieldData get(@Nonnull FormFieldDescriptor descriptor, @Nonnull Page<FormControlData> formControlData) {
        return new FormFieldData(descriptor, formControlData);
    }

    @Nonnull
    public FormFieldDescriptor getFormFieldDescriptor() {
        return formFieldDescriptor;
    }

    /**
     * Gets the page of form control values for this field.
     */
    @Nonnull
    public Page<FormControlData> getFormControlData() {
        return formControlData;
    }
}
