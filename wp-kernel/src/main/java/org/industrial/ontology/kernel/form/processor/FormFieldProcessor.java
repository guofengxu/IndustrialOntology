package org.industrial.ontology.kernel.form.processor;



import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormFieldBindingMissingException;
import javax.annotation.Nonnull;

import org.industrial.ontology.domain.form.data.FormFieldData;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormFieldProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class FormFieldProcessor {

    @Nonnull
    private final FormControlDataProcessor formControlDataProcessor;

    public FormFieldProcessor(@Nonnull FormControlDataProcessor formControlDataProcessor) {
        this.formControlDataProcessor = formControlDataProcessor;
    }

    public void processFormFieldData(@Nonnull FormFieldData formFieldData,
                              @Nonnull FormFrameBuilder formFrameBuilder) {
        var formFieldDescriptor = formFieldData.getFormFieldDescriptor();
        var owlBinding = formFieldDescriptor.getOwlBinding();
        var binding = owlBinding.orElseThrow(() -> new FormFieldBindingMissingException(formFieldDescriptor.getId()));
        var formControlData = formFieldData.getFormControlData();
        formControlData.forEach(fcd -> formControlDataProcessor.processFormControlData(binding, fcd, formFrameBuilder));
    }


}
