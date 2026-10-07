package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.ExpansionState;
import org.industrial.ontology.domain.form.HasFormFieldId;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormFieldDescriptorDto}.
 */
public record FormFieldDescriptorDto(@Nonnull @JsonProperty("id") FormFieldId id, @JsonIgnore @Nullable OwlBinding owlBindingInternal, @Nonnull LanguageMap label, @Nonnull FieldRun fieldRun, @Nonnull FormControlDescriptorDto formControlDescriptor, @Nonnull Optionality optionality, @Nonnull Repeatability repeatability, boolean readOnly, @Nonnull ExpansionState initialExpansionState, @Nonnull LanguageMap help) implements HasFormFieldId {

    public FormFieldDescriptorDto {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(fieldRun, "Null fieldRun");
        Objects.requireNonNull(formControlDescriptor, "Null formControlDescriptor");
        Objects.requireNonNull(optionality, "Null optionality");
        Objects.requireNonNull(repeatability, "Null repeatability");
        Objects.requireNonNull(initialExpansionState, "Null initialExpansionState");
        Objects.requireNonNull(help, "Null help");
    }

    @Nonnull
    public static FormFieldDescriptorDto get(FormFieldId formFieldId, OwlBinding owlBinding, LanguageMap newlabel, FieldRun fieldRun, FormControlDescriptorDto descriptorDto, Optionality optionality, Repeatability repeatability, boolean newReadOnly, ExpansionState initialExpansionState, LanguageMap help) {
        return new FormFieldDescriptorDto(formFieldId, owlBinding, newlabel, fieldRun, descriptorDto, optionality, repeatability, newReadOnly, initialExpansionState, help);
    }

    @Nonnull
    public Optional<OwlBinding> getOwlBinding() {
        return Optional.ofNullable(getOwlBindingInternal());
    }

    @JsonIgnore
    public boolean isComposite() {
        return getFormControlDescriptor() instanceof SubFormControlDescriptor;
    }

    public FormFieldDescriptor toFormFieldDescriptor() {
        return FormFieldDescriptor.get(getId(), getOwlBindingInternal(), getLabel(), getFieldRun(), getFormControlDescriptor().toFormControlDescriptor(), getRepeatability(), getOptionality(), isReadOnly(), getInitialExpansionState(), getHelp());
    }

    @Override
    @Nonnull
    @JsonProperty("id")
    public FormFieldId getId() {
        return id;
    }

    @JsonIgnore
    @Nullable
    public OwlBinding getOwlBindingInternal() {
        return owlBindingInternal;
    }

    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @Nonnull
    public FieldRun getFieldRun() {
        return fieldRun;
    }

    @Nonnull
    public FormControlDescriptorDto getFormControlDescriptor() {
        return formControlDescriptor;
    }

    @Nonnull
    public Optionality getOptionality() {
        return optionality;
    }

    @Nonnull
    public Repeatability getRepeatability() {
        return repeatability;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    @Nonnull
    public ExpansionState getInitialExpansionState() {
        return initialExpansionState;
    }

    @Nonnull
    public LanguageMap getHelp() {
        return help;
    }
}
