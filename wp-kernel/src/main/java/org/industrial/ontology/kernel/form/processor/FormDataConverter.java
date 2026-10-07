package org.industrial.ontology.kernel.form.processor;

import org.industrial.ontology.kernel.form.FormFrame;
import org.industrial.ontology.kernel.form.FormSubjectResolver;
import org.industrial.ontology.domain.frame.EntityFrame;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.domain.form.data.FormData;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormDataConverter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-13
 *
 * Converts raw {@link FormData} into a {@link FormFrame}, which is a step closer to an {@link EntityFrame}.
 */
public class FormDataConverter {

    @Nonnull
    private final FormSubjectResolver formSubjectResolver;

    @Nonnull
    private final FormDataProcessor formDataProcessor;

    public FormDataConverter(@Nonnull FormSubjectResolver formSubjectResolver, @Nonnull FormDataProcessor formDataProcessor) {
        this.formSubjectResolver = checkNotNull(formSubjectResolver);
        this.formDataProcessor = checkNotNull(formDataProcessor);
    }

    /**
     * Converts the specified {@link FormData} into its equivalent {@link FormFrame}.
     */
    public FormFrame convert(@Nonnull FormData formData) {
        var formFrameBuilder = formDataProcessor.processFormData(formData, false);
        return formFrameBuilder.build(formSubjectResolver);
    }
}
