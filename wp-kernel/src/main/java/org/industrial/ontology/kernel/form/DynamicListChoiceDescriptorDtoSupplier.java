package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;
import org.industrial.ontology.domain.form.field.ChoiceDescriptorDto;
import org.industrial.ontology.domain.form.field.DynamicChoiceListSourceDescriptor;
import org.industrial.ontology.domain.lang.LanguageMap;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

import static com.google.common.collect.ImmutableList.toImmutableList;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.DynamicListChoiceDescriptorDtoSupplier}.
 */
@FormDataBuilderSession
public class DynamicListChoiceDescriptorDtoSupplier {

    public static final int CHOICE_LIMIT = 100;
    @Nonnull
    private final MatchingEngine matchingEngine;

    @Nonnull
    private final FormDataBuilderSessionRenderer sessionRenderer;

    public DynamicListChoiceDescriptorDtoSupplier(@Nonnull MatchingEngine matchingEngine,
                                                  @Nonnull FormDataBuilderSessionRenderer sessionRenderer) {
        this.matchingEngine = checkNotNull(matchingEngine);
        this.sessionRenderer = checkNotNull(sessionRenderer);
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptorDto> getChoices(@Nonnull DynamicChoiceListSourceDescriptor descriptor) {
        var matchCriteria = descriptor.getCriteria();
        return matchingEngine.match(matchCriteria)
                             .map(sessionRenderer::getEntityRendering)
                             .sorted()
                             .limit(CHOICE_LIMIT)
                             .map(this::toChoiceDescriptorDto)
                             .collect(toImmutableList());
    }

    private ChoiceDescriptorDto toChoiceDescriptorDto(OWLEntityData entityData) {
        var shortForms = entityData.getShortForms();
        var dto = PrimitiveFormControlDataDto.get(entityData);
        var languageMap = LanguageMap.fromDictionaryMap(shortForms);
        return ChoiceDescriptorDto.get(dto, languageMap);
    }
}
