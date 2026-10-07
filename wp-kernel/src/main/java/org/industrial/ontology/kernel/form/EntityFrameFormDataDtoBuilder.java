package org.industrial.ontology.kernel.form;



import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.pagination.PageCollector;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormPageRequest;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.pagination.Page;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import static com.google.common.collect.ImmutableList.toImmutableList;

import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.FormDataDto;

import org.industrial.ontology.domain.form.data.FormFieldDataDto;
import org.industrial.ontology.domain.form.data.FormSubjectDto;
import org.industrial.ontology.domain.form.field.BoundControlDescriptor;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import org.industrial.ontology.domain.form.field.FormControlDescriptorVisitor;
import org.industrial.ontology.domain.form.field.FormRegionId;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.SubFormControlDescriptor;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.EntityFrameFormDataDtoBuilder}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-10-31
 */
@FormDataBuilderSession
public class EntityFrameFormDataDtoBuilder {

    @Nonnull
    private final FormDataBuilderSessionRenderer sessionRenderer;

    @Nonnull
    private final TextControlValuesBuilder textControlValuesBuilder;

    @Nonnull
    private final NumberControlValuesBuilder numberControlValuesBuilder;

    @Nonnull
    private final MultiChoiceControlValueBuilder multiChoiceControlValueBuilder;

    @Nonnull
    private final SingleChoiceControlValuesBuilder SingleChoiceControlValuesBuilder;

    @Nonnull
    private final EntityNameControlValuesBuilder entityNameControlValuesBuilder;

    @Nonnull
    private final ImageControlValuesBuilder imageControlValuesBuilder;

    @Nonnull
    private final GridControlValuesBuilder gridControlValuesBuilder;

    @Nonnull
    private final SubFormControlValuesBuilder subFormControlValuesBuilder;

    @Nonnull
    private final LangTagFilter langTagFilter;

    @Nonnull
    private final FormPageRequestIndex formPageRequestIndex;

    @Nonnull
    private final FormRegionFilterIndex formRegionFilterIndex;

    private final FormDescriptorDtoTranslator formDataDtoTranslator;

    public EntityFrameFormDataDtoBuilder(@Nonnull FormDataBuilderSessionRenderer sessionRenderer,
                                         @Nonnull TextControlValuesBuilder textControlValuesBuilder,
                                         @Nonnull NumberControlValuesBuilder numberControlValuesBuilder,
                                         @Nonnull MultiChoiceControlValueBuilder multiChoiceControlValueBuilder,
                                         @Nonnull SingleChoiceControlValuesBuilder singleChoiceControlValuesBuilder,
                                         @Nonnull EntityNameControlValuesBuilder entityNameControlValuesBuilder,
                                         @Nonnull ImageControlValuesBuilder imageControlValuesBuilder,
                                         @Nonnull GridControlValuesBuilder gridControlValuesBuilder,
                                         @Nonnull SubFormControlValuesBuilder subFormControlValuesBuilder,
                                         @Nonnull LangTagFilter langTagFilter,
                                         @Nonnull FormPageRequestIndex formPageRequestIndex,
                                         @Nonnull FormRegionFilterIndex formRegionFilterIndex, @Nonnull FormDescriptorDtoTranslator formDataDtoTranslator) {
        this.sessionRenderer = sessionRenderer;
        this.textControlValuesBuilder = textControlValuesBuilder;
        this.numberControlValuesBuilder = numberControlValuesBuilder;
        this.multiChoiceControlValueBuilder = multiChoiceControlValueBuilder;
        SingleChoiceControlValuesBuilder = singleChoiceControlValuesBuilder;
        this.entityNameControlValuesBuilder = entityNameControlValuesBuilder;
        this.imageControlValuesBuilder = imageControlValuesBuilder;
        this.gridControlValuesBuilder = gridControlValuesBuilder;
        this.subFormControlValuesBuilder = subFormControlValuesBuilder;
        this.langTagFilter = langTagFilter;
        this.formPageRequestIndex = formPageRequestIndex;
        this.formRegionFilterIndex = formRegionFilterIndex;
        this.formDataDtoTranslator = formDataDtoTranslator;
    }

    @Nonnull
    protected ImmutableList<FormControlDataDto> toFormControlValues(@Nonnull OWLEntityData subject,
                                                                    @Nonnull FormRegionId formFieldId,
                                                                    @Nonnull BoundControlDescriptor descriptor,
                                                                    int depth) {
        var owlBinding = descriptor.getOwlBinding();
        if (owlBinding.isEmpty()) {
            return ImmutableList.of();
        }
        var theBinding = owlBinding.get();
        var formControlDescriptor = descriptor.getFormControlDescriptor();
        return formControlDescriptor.accept(new FormControlDescriptorVisitor<>() {
            @Override
            public ImmutableList<FormControlDataDto> visit(TextControlDescriptor textControlDescriptor) {
                return textControlValuesBuilder.getTextControlDataDtoValues(textControlDescriptor,
                                                                            subject,
                                                                            theBinding, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(NumberControlDescriptor numberControlDescriptor) {
                return numberControlValuesBuilder.getNumberControlDataDtoValues(numberControlDescriptor,
                                                                                subject,
                                                                                theBinding, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(SingleChoiceControlDescriptor singleChoiceControlDescriptor) {
                return SingleChoiceControlValuesBuilder.getSingleChoiceControlDataDtoValues(singleChoiceControlDescriptor,
                                                                                            subject,
                                                                                            theBinding, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(MultiChoiceControlDescriptor multiChoiceControlDescriptor) {
                return multiChoiceControlValueBuilder.getMultiChoiceControlDataDtoValues(multiChoiceControlDescriptor,
                                                                                         subject,
                                                                                         theBinding,
                                                                                         depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(EntityNameControlDescriptor entityNameControlDescriptor) {
                return entityNameControlValuesBuilder.getEntityNameControlDataDtoValues(entityNameControlDescriptor,
                                                                                        subject,
                                                                                        theBinding, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(ImageControlDescriptor imageControlDescriptor) {
                return imageControlValuesBuilder.getImageControlDataDtoValues(imageControlDescriptor,
                                                                              subject,
                                                                              theBinding, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(GridControlDescriptor gridControlDescriptor) {
                return gridControlValuesBuilder.getGridControlDataDtoValues(gridControlDescriptor,
                                                                            subject,
                                                                            theBinding,
                                                                            formFieldId, depth);
            }

            @Override
            public ImmutableList<FormControlDataDto> visit(SubFormControlDescriptor subFormControlDescriptor) {
                return subFormControlValuesBuilder.getSubFormControlDataDtoValues(subFormControlDescriptor,
                                                                                  subject,
                                                                                  theBinding,
                                                                                  depth);
            }
        });
    }


    public FormDataDto toFormData(@Nonnull OWLEntity subject,
                                  @Nonnull FormDescriptor formDescriptor) {
        int depth = 0;
        return getFormDataDto(subject, formDescriptor, depth);
    }

    protected FormDataDto getFormDataDto(@Nonnull OWLEntity subject,
                                         @Nonnull FormDescriptor formDescriptor, int depth) {
        var subjectData = sessionRenderer.getEntityRendering(subject);
        var formSubject = FormSubjectDto.getFormSubject(subjectData);
        var fieldData = formDescriptor.getFields()
                                      .stream()
                                      .map(field -> {
                                          var formControlValues = toFormControlValues(subjectData,
                                                                                      field.getId(),
                                                                                      field,
                                                                                      depth);
                                          var controlValuesStream = formControlValues
                                                  .stream()
                                                  .filter(this::isIncluded);
                                          var pageRequest = formPageRequestIndex.getPageRequest(formSubject.toFormSubject(),
                                                                                                field.getId(),
                                                                                                FormPageRequest.SourceType.CONTROL_STACK);
                                          var controlValuesPage = controlValuesStream.collect(PageCollector.toPage(
                                                  pageRequest.getPageNumber(),
                                                  pageRequest.getPageSize()
                                          )).orElse(Page.emptyPage());
                                          var fieldDescriptorDto = formDataDtoTranslator.toFormFieldDescriptorDto(field);
                                          return FormFieldDataDto.get(fieldDescriptorDto, controlValuesPage);
                                      })
                                      .collect(toImmutableList());
        var formDescriptorDto = formDataDtoTranslator.toFormDescriptorDto(formDescriptor);
        return FormDataDto.get(formSubject, formDescriptorDto, fieldData, depth);
    }

    private boolean isIncluded(@Nonnull FormControlDataDto formControlData) {

        FormControlDataLangTagBasedInclusion formControlDataLangTagBasedInclusion = new FormControlDataLangTagBasedInclusion(
                langTagFilter);
        return formControlDataLangTagBasedInclusion.isIncluded(formControlData);

    }
}
