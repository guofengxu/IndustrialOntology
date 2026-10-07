package org.industrial.ontology.domain.form.field;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.SingleChoiceControlDescriptorDto}.
 */
public record SingleChoiceControlDescriptorDto(@Nonnull ImmutableList<ChoiceDescriptorDto> availableChoices, @Nonnull ChoiceListSourceDescriptor choiceListSourceDescriptor, @Nonnull SingleChoiceControlType widgetType) implements FormControlDescriptorDto {

    public SingleChoiceControlDescriptorDto {
        Objects.requireNonNull(availableChoices, "Null availableChoices");
        Objects.requireNonNull(choiceListSourceDescriptor, "Null choiceListSourceDescriptor");
        Objects.requireNonNull(widgetType, "Null widgetType");
    }

    @Nonnull
    public static SingleChoiceControlDescriptorDto get(@Nonnull SingleChoiceControlType widgetType, @Nonnull ImmutableList<ChoiceDescriptorDto> availableChoices, @Nonnull ChoiceListSourceDescriptor choiceListSourceDescriptor) {
        return new SingleChoiceControlDescriptorDto(availableChoices, choiceListSourceDescriptor, widgetType);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public SingleChoiceControlDescriptor toFormControlDescriptor() {
        return SingleChoiceControlDescriptor.get(getWidgetType(), getChoiceListSourceDescriptor());
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getAvailableChoices() {
        return availableChoices;
    }

    @Nonnull
    public ChoiceListSourceDescriptor getChoiceListSourceDescriptor() {
        return choiceListSourceDescriptor;
    }

    @Nonnull
    public SingleChoiceControlType getWidgetType() {
        return widgetType;
    }
}
