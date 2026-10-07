package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.EntityNameControlDataDto;
import org.industrial.ontology.kernel.form.data.EntityNameControlDataDtoComparator;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityNameControlValuesBuilder}.
 */
@FormDataBuilderSession
public class EntityNameControlValuesBuilder {

    @Nonnull
    private final BindingValuesExtractor bindingValuesExtractor;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex;

    @Nonnull
    private final FormDataBuilderSessionRenderer renderer;

    @Nonnull
    private final EntityNameControlDataDtoComparator comparator;

    public EntityNameControlValuesBuilder(@Nonnull BindingValuesExtractor bindingValuesExtractor,
                                          @Nonnull EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex,
                                          @Nonnull FormDataBuilderSessionRenderer renderer,
                                          @Nonnull EntityNameControlDataDtoComparator comparator) {
        this.bindingValuesExtractor = bindingValuesExtractor;
        this.entitiesInProjectSignatureByIriIndex = entitiesInProjectSignatureByIriIndex;
        this.renderer = renderer;
        this.comparator = comparator;
    }

    @Nonnull
    public ImmutableList<FormControlDataDto> getEntityNameControlDataDtoValues(@Nonnull EntityNameControlDescriptor entityNameControlDescriptor,
                                                                               @Nonnull OWLEntityData subject,
                                                                               @Nonnull OwlBinding theBinding,
                                                                               int depth) {
        var values = bindingValuesExtractor.getBindingValues(subject.getEntity(), theBinding);
        return values.stream()
                     // Allow IRIs which correspond to entities
                     .filter(p -> p instanceof OWLEntity || p instanceof IRI)
                     .flatMap(p -> {
                         if (p instanceof OWLEntity) {
                             return Stream.of((OWLEntity) p);
                         } else {
                             var iri = (IRI) p;
                             return entitiesInProjectSignatureByIriIndex.getEntitiesInSignature(iri);
                         }
                     })
                     .map(renderer::getEntityRendering)
                     .map(entity -> EntityNameControlDataDto.get(entityNameControlDescriptor, entity, depth))
                     .sorted(comparator)
                     .collect(ImmutableList.toImmutableList());
    }
}
