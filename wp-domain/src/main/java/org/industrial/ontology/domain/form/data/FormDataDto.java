package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.FormDescriptorDto;
import org.industrial.ontology.domain.form.FormId;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormDataDto}.
 */
public record FormDataDto(int depth, @JsonIgnore @Nullable FormSubjectDto subjectInternal, FormDescriptorDto formDescriptor, ImmutableList<FormFieldDataDto> formFieldData) implements FormControlDataDto {

    public FormDataDto {
        Objects.requireNonNull(formDescriptor, "Null formDescriptor");
        Objects.requireNonNull(formFieldData, "Null formFieldData");
    }

    @Nonnull
    public static FormDataDto get(@Nonnull FormSubjectDto subject, @Nonnull FormDescriptorDto formDescriptor, @Nonnull ImmutableList<FormFieldDataDto> formFieldData, int depth) {
        return new FormDataDto(depth, subject, formDescriptor, formFieldData);
    }

    @Nonnull
    public Optional<FormSubjectDto> getSubject() {
        return Optional.ofNullable(getSubjectInternal());
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public FormControlData toFormControlData() {
        return FormData.get(getSubject().map(FormSubjectDto::toFormSubject), getFormDescriptor().toFormDescriptor(), getFormFieldData().stream().map(FormFieldDataDto::getFormFieldData).collect(toImmutableList()));
    }

    @Nonnull
    public FormData toFormData() {
        return FormData.get(getSubject().map(FormSubjectDto::toFormSubject), getFormDescriptor().toFormDescriptor(), getFormFieldData().stream().map(FormFieldDataDto::toFormFieldData).collect(toImmutableList()));
    }

    @Nonnull
    public FormId getFormId() {
        return getFormDescriptor().getFormId();
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @JsonIgnore
    @Nullable
    public FormSubjectDto getSubjectInternal() {
        return subjectInternal;
    }

    public FormDescriptorDto getFormDescriptor() {
        return formDescriptor;
    }

    public ImmutableList<FormFieldDataDto> getFormFieldData() {
        return formFieldData;
    }
}
