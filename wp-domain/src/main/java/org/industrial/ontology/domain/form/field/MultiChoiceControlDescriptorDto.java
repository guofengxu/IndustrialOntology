package org.industrial.ontology.domain.form.field;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.MultiChoiceControlDescriptorDto}.
 */
public record MultiChoiceControlDescriptorDto(@Nonnull ChoiceListSourceDescriptor choiceListSourceDescriptor, @Nonnull ImmutableList<ChoiceDescriptorDto> availableChoices) implements FormControlDescriptorDto {

    public MultiChoiceControlDescriptorDto {
        Objects.requireNonNull(choiceListSourceDescriptor, "Null choiceListSourceDescriptor");
        Objects.requireNonNull(availableChoices, "Null availableChoices");
    }

    @Nonnull
    public static MultiChoiceControlDescriptorDto get(@Nonnull ChoiceListSourceDescriptor choiceListSourceDescriptor, @Nonnull ImmutableList<ChoiceDescriptorDto> choices) {
        return new MultiChoiceControlDescriptorDto(choiceListSourceDescriptor, choices);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public MultiChoiceControlDescriptor toFormControlDescriptor() {
        return MultiChoiceControlDescriptor.get(getChoiceListSourceDescriptor(), ImmutableList.of());
    }

    @Nonnull
    public ChoiceListSourceDescriptor getChoiceListSourceDescriptor() {
        return choiceListSourceDescriptor;
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getAvailableChoices() {
        return availableChoices;
    }
}
