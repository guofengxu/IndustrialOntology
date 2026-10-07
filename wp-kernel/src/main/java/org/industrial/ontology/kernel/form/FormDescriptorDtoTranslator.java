package org.industrial.ontology.kernel.form;



import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormDescriptorDto;
import javax.annotation.Nonnull;

import static com.google.common.collect.ImmutableList.toImmutableList;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;

import org.industrial.ontology.domain.form.field.EntityNameControlDescriptorDto;
import org.industrial.ontology.domain.form.field.FormControlDescriptor;
import org.industrial.ontology.domain.form.field.FormControlDescriptorDto;
import org.industrial.ontology.domain.form.field.FormControlDescriptorVisitor;
import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.form.field.FormFieldDescriptorDto;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnDescriptorDto;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.GridControlDescriptorDto;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.industrial.ontology.domain.form.field.ImageControlDescriptorDto;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptorDto;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlDescriptorDto;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptorDto;
import org.industrial.ontology.domain.form.field.SubFormControlDescriptor;
import org.industrial.ontology.domain.form.field.SubFormControlDescriptorDto;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.industrial.ontology.domain.form.field.TextControlDescriptorDto;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormDescriptorDtoTranslator}.
 */
public class FormDescriptorDtoTranslator {

    @Nonnull
    private final FormControlDescriptorVisitor<FormControlDescriptorDto> translatorVisitor;

    public FormDescriptorDtoTranslator(@Nonnull ChoiceDescriptorCache choiceDescriptorCache) {
        this.translatorVisitor = new FormControlDescriptorVisitor<>() {
            @Override
            public FormControlDescriptorDto visit(TextControlDescriptor textControlDescriptor) {
                return TextControlDescriptorDto.get(textControlDescriptor);
            }

            @Override
            public FormControlDescriptorDto visit(NumberControlDescriptor numberControlDescriptor) {
                return NumberControlDescriptorDto.get(numberControlDescriptor);
            }

            @Override
            public FormControlDescriptorDto visit(SingleChoiceControlDescriptor singleChoiceControlDescriptor) {
                var choices = choiceDescriptorCache.getChoices(singleChoiceControlDescriptor.getSource());

                return SingleChoiceControlDescriptorDto.get(singleChoiceControlDescriptor.getWidgetType(),
                                                            choices, singleChoiceControlDescriptor.getSource());
            }

            @Override
            public FormControlDescriptorDto visit(MultiChoiceControlDescriptor multiChoiceControlDescriptor) {
                var choices = choiceDescriptorCache.getChoices(multiChoiceControlDescriptor.getSource());
                return MultiChoiceControlDescriptorDto.get(multiChoiceControlDescriptor.getSource(), choices);
            }

            @Override
            public FormControlDescriptorDto visit(EntityNameControlDescriptor entityNameControlDescriptor) {
                return EntityNameControlDescriptorDto.get(entityNameControlDescriptor.getPlaceholder(),
                                                          entityNameControlDescriptor.getMatchCriteria().orElse(null));
            }

            @Override
            public FormControlDescriptorDto visit(ImageControlDescriptor imageControlDescriptor) {
                return ImageControlDescriptorDto.get();
            }

            @Override
            public FormControlDescriptorDto visit(GridControlDescriptor gridControlDescriptor) {
                var columns = gridControlDescriptor.getColumns()
                                                   .stream()
                                                   .map(this::toGridColumnDescriptorDto)
                                                   .collect(toImmutableList());
                return GridControlDescriptorDto.get(columns, gridControlDescriptor.getSubjectFactoryDescriptor().orElseThrow());
            }

            private GridColumnDescriptorDto toGridColumnDescriptorDto(GridColumnDescriptor column) {
                return GridColumnDescriptorDto.get(column.getId(),
                                                   column.getOptionality(),
                                                   column.getRepeatability(),
                                                   column.getOwlBinding().orElseThrow(),
                                                   column.getLabel(),
                                                   column.getFormControlDescriptor().accept(this));
            }

            @Override
            public FormControlDescriptorDto visit(SubFormControlDescriptor subFormControlDescriptor) {
                return SubFormControlDescriptorDto.get(toFormDescriptorDto(subFormControlDescriptor.getFormDescriptor()));
            }
        };
    }

    @Nonnull
    public FormFieldDescriptorDto toFormFieldDescriptorDto(@Nonnull FormFieldDescriptor descriptor) {
        return FormFieldDescriptorDto.get(descriptor.getId(),
                                          descriptor.getOwlBinding().orElse(null),
                                          descriptor.getLabel(),
                                          descriptor.getFieldRun(),
                                          toFormControlDescriptorDto(descriptor.getFormControlDescriptor()),
                                          descriptor.getOptionality(),
                                          descriptor.getRepeatability(),
                                          descriptor.isReadOnly(),
                                          descriptor.getInitialExpansionState(),
                                          descriptor.getHelp());
    }

    @Nonnull
    public FormDescriptorDto toFormDescriptorDto(FormDescriptor descriptor) {
        var fields = descriptor.getFields().stream().map(this::toFormFieldDescriptorDto).collect(toImmutableList());
        return FormDescriptorDto.get(descriptor.getFormId(),
                                     descriptor.getLabel(),
                                     fields,
                                     descriptor.getSubjectFactoryDescriptor().orElse(null));
    }

    @Nonnull
    public FormControlDescriptorDto toFormControlDescriptorDto(@Nonnull FormControlDescriptor descriptor) {
        return descriptor.accept(translatorVisitor);
    }
}
