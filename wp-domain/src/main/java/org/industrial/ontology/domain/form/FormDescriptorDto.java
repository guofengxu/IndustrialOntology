package org.industrial.ontology.domain.form;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.FormFieldDescriptorDto;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormDescriptorDto}.
 */
public record FormDescriptorDto(@Nonnull FormId formId, @Nonnull LanguageMap label, @Nonnull ImmutableList<FormFieldDescriptorDto> fields, @Nullable FormSubjectFactoryDescriptor formSubjectFactoryDescriptorInternal) {

    public FormDescriptorDto {
        Objects.requireNonNull(formId, "Null formId");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(fields, "Null fields");
    }

    public static FormDescriptorDto get(@Nonnull FormId formId, @Nonnull LanguageMap label, @Nonnull ImmutableList<FormFieldDescriptorDto> fields, @Nullable FormSubjectFactoryDescriptor subjectFactoryDescriptor) {
        return new FormDescriptorDto(formId, label, fields, subjectFactoryDescriptor);
    }

    public Optional<FormSubjectFactoryDescriptor> getFormSubjectFactoryDescriptor() {
        return Optional.ofNullable(getFormSubjectFactoryDescriptorInternal());
    }

    @Nonnull
    public FormDescriptor toFormDescriptor() {
        return new FormDescriptor(getFormId(), getLabel(), getFields().stream().map(FormFieldDescriptorDto::toFormFieldDescriptor).collect(toImmutableList()), getFormSubjectFactoryDescriptor());
    }

    @Nonnull
    public FormId getFormId() {
        return formId;
    }

    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @Nonnull
    public ImmutableList<FormFieldDescriptorDto> getFields() {
        return fields;
    }

    @Nullable
    public FormSubjectFactoryDescriptor getFormSubjectFactoryDescriptorInternal() {
        return formSubjectFactoryDescriptorInternal;
    }
}
