package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.ImageControlDataDto;
import org.industrial.ontology.kernel.form.data.ImageControlDataDtoComparator;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.semanticweb.owlapi.model.IRI;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import static com.google.common.collect.ImmutableList.toImmutableList;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.ImageControlValuesBuilder}.
 */
@FormDataBuilderSession
public class ImageControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final ImageControlDataDtoComparator comparator;

    public ImageControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor, @Nonnull ImageControlDataDtoComparator imageControlDataDtoComparator) {
        this.bindingValuesExtractor = checkNotNull(bindingValuesExtractor);
        this.comparator = checkNotNull(imageControlDataDtoComparator);
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getImageControlDataDtoValues(ImageControlDescriptor imageControlDescriptor, @Nonnull OWLEntityData subject, OwlBinding theBinding, int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return values.stream()
                     .filter(p -> p instanceof IRI)
                     .map(p -> (IRI) p)
                     .map(iri -> ImageControlDataDto.get(imageControlDescriptor, iri, depth))
                     .sorted(comparator)
                     .collect(toImmutableList());
    }
}
