package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.SubFormControlDescriptor;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.SubFormControlValuesBuilder}.
 */
@FormDataBuilderSession
public class SubFormControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final Supplier<EntityFrameFormDataDtoBuilder> formDataDtoBuilderProvider;

    public SubFormControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor, @Nonnull Supplier<EntityFrameFormDataDtoBuilder> formDataDtoBuilderProvider) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.formDataDtoBuilderProvider = checkNotNull(formDataDtoBuilderProvider);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getSubFormControlDataDtoValues(SubFormControlDescriptor subFormControlDescriptor, @Nonnull OWLEntityData subject, @Nonnull OwlBinding theBinding, int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        FormDescriptor subFormDescriptor = subFormControlDescriptor.getFormDescriptor();
        return values.stream().filter(p -> p instanceof OWLEntity).map(p -> (OWLEntity) p).map(entity -> formDataDtoBuilderProvider.get().getFormDataDto(entity, subFormDescriptor, depth)).collect(ImmutableList.toImmutableList());
    }
}
