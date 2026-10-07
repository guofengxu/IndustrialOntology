package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;
import org.industrial.ontology.domain.form.data.SingleChoiceControlDataDto;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.SingleChoiceControlValuesBuilder}.
 */
@FormDataBuilderSession
public class SingleChoiceControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final PrimitiveFormControlDataDtoRenderer renderer;

    @Nonnull
    private final LangTagFilter langTagFilter;

    public SingleChoiceControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                            @Nonnull PrimitiveFormControlDataDtoRenderer renderer,
                                            @Nonnull LangTagFilter langTagFilter) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.renderer = checkNotNull(renderer);
        this.langTagFilter = checkNotNull(langTagFilter);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getSingleChoiceControlDataDtoValues(@Nonnull SingleChoiceControlDescriptor singleChoiceControlDescriptor,
                                                                                 @Nonnull OWLEntityData subject,
                                                                                 @Nonnull OwlBinding theBinding, int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return values.stream()
                         .flatMap(renderer::toFormControlDataDto)
                         .filter(this::isIncluded)
                         .map(value -> SingleChoiceControlDataDto.get(
                                 singleChoiceControlDescriptor,
                                 value,
                                 depth))
                         .collect(ImmutableList.<FormControlDataDto>toImmutableList());
    }

    private boolean isIncluded(@Nonnull PrimitiveFormControlDataDto dto) {
        var primitive = dto.toPrimitiveFormControlData().getPrimitive();
        if (primitive instanceof OWLLiteral) {
            return langTagFilter.isIncluded(((OWLLiteral) primitive).getLang());
        } else {
            return true;
        }
    }
}
