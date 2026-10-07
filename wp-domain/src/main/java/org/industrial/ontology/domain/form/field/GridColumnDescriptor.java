package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.GridColumnDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-24
 */
public record GridColumnDescriptor(@Nonnull GridColumnId id, @Nonnull Optionality optionality, @Nonnull Repeatability repeatability, @JsonIgnore @Nullable OwlBinding owlBindingInternal, @Nonnull LanguageMap label, @Nonnull FormControlDescriptor formControlDescriptor) implements BoundControlDescriptor {

    public GridColumnDescriptor {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(optionality, "Null optionality");
        Objects.requireNonNull(repeatability, "Null repeatability");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(formControlDescriptor, "Null formControlDescriptor");
    }

    @JsonCreator
    @Nonnull
    public static GridColumnDescriptor get(@Nonnull @JsonProperty("id") GridColumnId id, @Nullable @JsonProperty("optionality") Optionality optionality, @Nullable @JsonProperty("repeatability") Repeatability repeatability, @Nullable @JsonProperty("owlBinding") OwlBinding owlBinding, @Nonnull @JsonProperty("label") LanguageMap columnLabel, @Nonnull @JsonProperty("formControlDescriptor") FormControlDescriptor formControlDescriptor) {
        return new GridColumnDescriptor(id, optionality == null ? Optionality.REQUIRED : optionality, repeatability == null ? Repeatability.NON_REPEATABLE : repeatability, owlBinding, columnLabel, formControlDescriptor);
    }

    @JsonIgnore
    public Stream<GridColumnDescriptor> getLeafColumnDescriptors() {
        FormControlDescriptor formControlDescriptor = getFormControlDescriptor();
        if (formControlDescriptor instanceof GridControlDescriptor) {
            // This is not a leaf column
            return ((GridControlDescriptor) formControlDescriptor).getLeafColumns();
        } else {
            // This is a leaf column
            return Stream.of(this);
        }
    }

    @JsonIgnore
    public Stream<GridColumnId> getLeafColumnIds() {
        return getLeafColumnDescriptors().map(GridColumnDescriptor::getId);
    }

    @JsonIgnore
    public boolean isLeafColumnDescriptor() {
        return !(getFormControlDescriptor() instanceof GridControlDescriptor);
    }

    @Override
    @Nonnull
    public Optional<OwlBinding> getOwlBinding() {
        return Optional.ofNullable(getOwlBindingInternal());
    }

    @JsonIgnore
    public int getNestedColumnCount() {
        FormControlDescriptor formControlDescriptor = getFormControlDescriptor();
        if (formControlDescriptor instanceof GridControlDescriptor) {
            return ((GridControlDescriptor) getFormControlDescriptor()).getNestedColumnCount();
        } else {
            return 1;
        }
    }

    @JsonIgnore
    public boolean isRepeatable() {
        return getRepeatability().isRepeatable();
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

    @Override
    @Nonnull
    public FormControlDescriptor getFormControlDescriptor() {
        return formControlDescriptor;
    }
}
