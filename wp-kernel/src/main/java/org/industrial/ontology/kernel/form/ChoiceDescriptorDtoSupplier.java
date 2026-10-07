package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.ChoiceDescriptorDto;
import org.industrial.ontology.domain.form.field.ChoiceListSourceDescriptor;
import org.industrial.ontology.domain.form.field.DynamicChoiceListSourceDescriptor;
import org.industrial.ontology.domain.form.field.FixedChoiceListSourceDescriptor;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.ChoiceDescriptorDtoSupplier}.
 */
public class ChoiceDescriptorDtoSupplier {

    @Nonnull
    private final FixedListChoiceDescriptorDtoSupplier fixedListSupplier;

    @Nonnull
    private final DynamicListChoiceDescriptorDtoSupplier dynamicListSupplier;

    public ChoiceDescriptorDtoSupplier(@Nonnull FixedListChoiceDescriptorDtoSupplier fixedListSupplier, @Nonnull DynamicListChoiceDescriptorDtoSupplier dynamicListSupplier, @Nonnull FormDataBuilderSessionRenderer sessionRenderer) {
        this.fixedListSupplier = checkNotNull(fixedListSupplier);
        this.dynamicListSupplier = checkNotNull(dynamicListSupplier);
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getChoices(@Nonnull ChoiceListSourceDescriptor descriptor) {
        checkNotNull(descriptor);
        if(descriptor instanceof FixedChoiceListSourceDescriptor) {
            return fixedListSupplier.getChoices((FixedChoiceListSourceDescriptor) descriptor);
        }
        else {
            return dynamicListSupplier.getChoices((DynamicChoiceListSourceDescriptor) descriptor);
        }
    }
}
