package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.NumberControlDataDto;
import org.industrial.ontology.kernel.form.data.NumberControlDataDtoComparator;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.NumberControlValuesBuilder}.
 */
@FormDataBuilderSession
public class NumberControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final NumberControlDataDtoComparator comparator;

    public NumberControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                      @Nonnull NumberControlDataDtoComparator comparator) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.comparator = checkNotNull(comparator);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getNumberControlDataDtoValues(NumberControlDescriptor numberControlDescriptor, @Nonnull OWLEntityData subject, OwlBinding theBinding, int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return values.stream()
                     .filter(p -> p instanceof OWLLiteral)
                     .map(p -> (OWLLiteral) p)
                     .map(value -> NumberControlDataDto.get(numberControlDescriptor, value, depth))
                     .sorted(comparator)
                     .collect(ImmutableList.toImmutableList());
    }
}
