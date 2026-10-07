package org.industrial.ontology.kernel.form.processor;

import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptorMissingException;
import org.industrial.ontology.domain.form.data.FormData;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormDataProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class FormDataProcessor {

    @Nonnull
    private final Supplier<FormFrameBuilder> formFrameBuilderProvider;

    @Nonnull
    private final FormFieldProcessor formFieldProcessor;

    public FormDataProcessor(@Nonnull Supplier<FormFrameBuilder> formFrameBuilderProvider, @Nonnull FormFieldProcessor formFieldProcessor) {
        this.formFrameBuilderProvider = checkNotNull(formFrameBuilderProvider);
        this.formFieldProcessor = checkNotNull(formFieldProcessor);
    }

    public FormFrameBuilder processFormData(@Nonnull FormData formData, boolean subjectFactoryDescriptorRequired) {
        var formFrameBuilder = formFrameBuilderProvider.get();
        formData.getSubject().ifPresent(formFrameBuilder::setSubject);
        if (subjectFactoryDescriptorRequired) {
            var formSubjectFactoryDescriptor = formData.getFormDescriptor().getSubjectFactoryDescriptor().orElseThrow(FormSubjectFactoryDescriptorMissingException::new);
            formFrameBuilder.setSubjectFactoryDescriptor(formSubjectFactoryDescriptor);
        } else {
            formData.getFormDescriptor().getSubjectFactoryDescriptor().ifPresent(formFrameBuilder::setSubjectFactoryDescriptor);
        }
        formData.getFormFieldData().forEach(formFieldData -> formFieldProcessor.processFormFieldData(formFieldData, formFrameBuilder));
        return formFrameBuilder;
    }
}
