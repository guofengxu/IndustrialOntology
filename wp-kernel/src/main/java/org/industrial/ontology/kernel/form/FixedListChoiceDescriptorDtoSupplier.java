package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.ChoiceDescriptor;
import org.industrial.ontology.domain.form.field.ChoiceDescriptorDto;
import org.industrial.ontology.domain.form.field.FixedChoiceListSourceDescriptor;
import javax.annotation.Nonnull;

import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;

import static com.google.common.collect.ImmutableList.toImmutableList;

import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FixedListChoiceDescriptorDtoSupplier}.
 */
public class FixedListChoiceDescriptorDtoSupplier {

    @Nonnull
    private final PrimitiveFormControlDataDtoRenderer renderer;

    public FixedListChoiceDescriptorDtoSupplier(@Nonnull PrimitiveFormControlDataDtoRenderer renderer) {
        this.renderer = checkNotNull(renderer);
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getChoices(@Nonnull FixedChoiceListSourceDescriptor descriptor) {
        return descriptor.getChoices()
                  .stream()
                  .flatMap(choiceDescriptor -> descriptor.getChoices().stream())
                  .flatMap(this::toChoiceDescriptorDto)
                  .collect(toImmutableList());
    }

    @Nonnull
    private Stream<ChoiceDescriptorDto> toChoiceDescriptorDto(ChoiceDescriptor choiceDescriptor) {
        return toPrimitiveFormControlDataDto(choiceDescriptor.getValue())
                .map(dto -> ChoiceDescriptorDto.get(dto, choiceDescriptor.getLabel()));
    }

    @Nonnull
    private Stream<PrimitiveFormControlDataDto> toPrimitiveFormControlDataDto(@Nonnull PrimitiveFormControlData data) {
        var primitive = data.getPrimitive();
        return renderer.toFormControlDataDto(primitive);
    }
}
