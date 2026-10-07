package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.MultiChoiceControlDataDto;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.MultiChoiceControlValueBuilder}.
 */
@FormDataBuilderSession
public class MultiChoiceControlValueBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final PrimitiveFormControlDataDtoRenderer renderer;

    public MultiChoiceControlValueBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                          @Nonnull PrimitiveFormControlDataDtoRenderer renderer) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.renderer = checkNotNull(renderer);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getMultiChoiceControlDataDtoValues(@Nonnull MultiChoiceControlDescriptor multiChoiceControlDescriptor,
                                                                         @Nonnull OWLEntityData subject,
                                                                         @Nonnull OwlBinding theBinding,
                                                                                int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        var vals = values.stream()
                         .flatMap(renderer::toFormControlDataDto)
                         .collect(ImmutableList.toImmutableList());
        return ImmutableList.of(MultiChoiceControlDataDto.get(multiChoiceControlDescriptor,
                                                              vals,
                                                              depth));
    }
}
