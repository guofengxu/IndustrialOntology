package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.form.ExpansionState;
import org.industrial.ontology.domain.form.HasFormFieldId;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.FIELD_RUN;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.FORM_CONTROL_DESCRIPTOR;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.HELP;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.ID;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.INITIAL_EXPANSIONS_STATE;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.LABEL;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.OPTIONALITY;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.OWL_BINDING;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.READ_ONLY;
import static org.industrial.ontology.domain.form.field.FormFieldDescriptor.REPEATABILITY;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormFieldDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
@JsonPropertyOrder({ ID, OWL_BINDING, LABEL, FIELD_RUN, FORM_CONTROL_DESCRIPTOR, REPEATABILITY, OPTIONALITY, READ_ONLY, HELP })
public record FormFieldDescriptor(@Nonnull @JsonProperty("id") FormFieldId id, @JsonIgnore @Nullable OwlBinding owlBindingInternal, @Nonnull LanguageMap label, @Nonnull FieldRun fieldRun, @Nonnull FormControlDescriptor formControlDescriptor, @Nonnull Optionality optionality, @Nonnull Repeatability repeatability, boolean readOnly, @JsonProperty(FormFieldDescriptor.INITIAL_EXPANSIONS_STATE) @Nonnull ExpansionState initialExpansionState, @Nonnull LanguageMap help) implements HasFormFieldId, HasRepeatability, BoundControlDescriptor {

    public FormFieldDescriptor {
        Objects.requireNonNull(id, "Null id");
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(fieldRun, "Null fieldRun");
        Objects.requireNonNull(formControlDescriptor, "Null formControlDescriptor");
        Objects.requireNonNull(optionality, "Null optionality");
        Objects.requireNonNull(repeatability, "Null repeatability");
        Objects.requireNonNull(initialExpansionState, "Null initialExpansionState");
        Objects.requireNonNull(help, "Null help");
    }

    public static final String ID = "id";

    public static final String OWL_BINDING = "owlBinding";

    public static final String LABEL = "label";

    public static final String FIELD_RUN = "fieldRun";

    public static final String FORM_CONTROL_DESCRIPTOR = "formControlDescriptor";

    public static final String REPEATABILITY = "repeatability";

    public static final String OPTIONALITY = "optionality";

    public static final String READ_ONLY = "readOnly";

    public static final String INITIAL_EXPANSIONS_STATE = "initialExpansionState";

    public static final String HELP = "help";

    @JsonCreator
    @Nonnull
    public static FormFieldDescriptor get(@JsonProperty(ID) @Nonnull FormFieldId id, @JsonProperty(OWL_BINDING) @Nullable OwlBinding owlBinding, @JsonProperty(LABEL) @Nullable LanguageMap formLabel, @JsonProperty(FIELD_RUN) @Nullable FieldRun fieldRun, @JsonProperty(FORM_CONTROL_DESCRIPTOR) @Nonnull FormControlDescriptor fieldDescriptor, @JsonProperty(REPEATABILITY) @Nullable Repeatability repeatability, @JsonProperty(OPTIONALITY) @Nullable Optionality optionality, @JsonProperty(READ_ONLY) boolean readOnly, @JsonProperty(INITIAL_EXPANSIONS_STATE) @Nullable ExpansionState expansionState, @JsonProperty(HELP) @Nullable LanguageMap help) {
        return new FormFieldDescriptor(id, owlBinding, formLabel == null ? LanguageMap.empty() : formLabel, fieldRun == null ? FieldRun.START : fieldRun, fieldDescriptor, optionality == null ? Optionality.REQUIRED : optionality, repeatability == null ? Repeatability.NON_REPEATABLE : repeatability, readOnly, expansionState == null ? ExpansionState.EXPANDED : expansionState, help == null ? LanguageMap.empty() : help);
    }

    @Override
    @Nonnull
    public Optional<OwlBinding> getOwlBinding() {
        return Optional.ofNullable(getOwlBindingInternal());
    }

    @JsonIgnore
    public boolean isComposite() {
        return getFormControlDescriptor() instanceof SubFormControlDescriptor;
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

    @Override
    @Nonnull
    public FormControlDescriptor getFormControlDescriptor() {
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

    @JsonProperty(INITIAL_EXPANSIONS_STATE)
    @Nonnull
    public ExpansionState getInitialExpansionState() {
        return initialExpansionState;
    }

    @Nonnull
    public LanguageMap getHelp() {
        return help;
    }
}
