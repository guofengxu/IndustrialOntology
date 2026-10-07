package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.GridColumnDescriptorDto}.
 */
public record GridColumnDescriptorDto(@Nonnull GridColumnId id, @Nonnull Optionality optionality, @Nonnull Repeatability repeatability, @JsonIgnore @Nullable OwlBinding owlBindingInternal, @Nonnull LanguageMap label, @Nonnull FormControlDescriptorDto formControlDescriptor) {

    public GridColumnDescriptorDto {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(optionality, "Null optionality");
        Objects.requireNonNull(repeatability, "Null repeatability");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(formControlDescriptor, "Null formControlDescriptor");
    }

    @Nonnull
    public static GridColumnDescriptorDto get(@Nonnull GridColumnId columnId, @Nonnull Optionality optionality, @Nonnull Repeatability repeatability, @Nullable OwlBinding binding, @Nonnull LanguageMap label, @Nonnull FormControlDescriptorDto formControlDescriptorDto) {
        return new GridColumnDescriptorDto(columnId, optionality, repeatability, binding, label, formControlDescriptorDto);
    }

    @Nonnull
    public Optional<OwlBinding> getOwlBinding() {
        return Optional.ofNullable(getOwlBindingInternal());
    }

    public GridColumnDescriptor toGridColumnDescriptor() {
        return GridColumnDescriptor.get(getId(), getOptionality(), getRepeatability(), getOwlBindingInternal(), getLabel(), getFormControlDescriptor().toFormControlDescriptor());
    }

    @JsonIgnore
    public int getNestedColumnCount() {
        FormControlDescriptorDto formControlDescriptor = getFormControlDescriptor();
        if (formControlDescriptor instanceof GridControlDescriptorDto) {
            return ((GridControlDescriptorDto) getFormControlDescriptor()).getNestedColumnCount();
        } else {
            return 1;
        }
    }

    @JsonIgnore
    public Stream<GridColumnDescriptorDto> getLeafColumnDescriptors() {
        FormControlDescriptorDto formControlDescriptor = getFormControlDescriptor();
        if (formControlDescriptor instanceof GridControlDescriptorDto) {
            // This is not a leaf column
            return ((GridControlDescriptorDto) formControlDescriptor).getLeafColumns();
        } else {
            // This is a leaf column
            return Stream.of(this);
        }
    }

    @JsonIgnore
    public boolean isLeafColumnDescriptor() {
        return !(getFormControlDescriptor() instanceof GridControlDescriptorDto);
    }

    @Nonnull
    public GridColumnId getId() {
        return id;
    }

    @Nonnull
    public Optionality getOptionality() {
        return optionality;
    }

    @Nonnull
    public Repeatability getRepeatability() {
        return repeatability;
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
    public FormControlDescriptorDto getFormControlDescriptor() {
        return formControlDescriptor;
    }
}
