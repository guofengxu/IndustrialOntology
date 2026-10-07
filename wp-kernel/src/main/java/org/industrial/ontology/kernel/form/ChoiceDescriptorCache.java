package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.ChoiceDescriptorDto;
import org.industrial.ontology.domain.form.field.ChoiceListSourceDescriptor;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.ChoiceDescriptorCache}.
 */
@FormDataBuilderSession
public class ChoiceDescriptorCache {


    @Nonnull
    private final Map<ChoiceListSourceDescriptor, ImmutableList<ChoiceDescriptorDto>> descriptorCache = new HashMap<>();

    @Nonnull
    private final ChoiceDescriptorDtoSupplier choiceDescriptorDtoSupplier;

    public ChoiceDescriptorCache(@Nonnull ChoiceDescriptorDtoSupplier choiceDescriptorDtoSupplier) {
        this.choiceDescriptorDtoSupplier = checkNotNull(choiceDescriptorDtoSupplier);
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getChoices(@Nonnull ChoiceListSourceDescriptor sourceDescriptor) {
        var cachedChoices = descriptorCache.get(sourceDescriptor);
        if (cachedChoices == null) {
            cachedChoices = choiceDescriptorDtoSupplier.getChoices(sourceDescriptor);
            descriptorCache.put(sourceDescriptor, cachedChoices);
        }
        return cachedChoices;
    }
}
