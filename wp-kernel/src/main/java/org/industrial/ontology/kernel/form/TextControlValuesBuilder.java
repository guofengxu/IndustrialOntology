package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.TextControlDataDto;
import org.industrial.ontology.kernel.form.data.TextControlDataDtoComparator;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.TextControlValuesBuilder}.
 */
@FormDataBuilderSession
public class TextControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final TextControlDataDtoComparator textControlDataDtoComparator;

    public TextControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                    @Nonnull TextControlDataDtoComparator textControlDataDtoComparator) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.textControlDataDtoComparator = checkNotNull(textControlDataDtoComparator);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getTextControlDataDtoValues(@Nonnull TextControlDescriptor textControlDescriptor,
                                                                         @Nonnull OWLEntityData subject,
                                                                         @Nonnull OwlBinding theBinding,
                                                                         int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return values.stream()
                     .filter(p -> p instanceof OWLLiteral)
                     .map(p -> (OWLLiteral) p)
                     .map(literal -> TextControlDataDto.get(textControlDescriptor, literal, depth))
                     .sorted(textControlDataDtoComparator)
                     .collect(ImmutableList.toImmutableList());
    }
}
