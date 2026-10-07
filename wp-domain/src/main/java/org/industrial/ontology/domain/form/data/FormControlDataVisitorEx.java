package org.industrial.ontology.domain.form.data;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormControlDataVisitorEx}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-13
 */
public interface FormControlDataVisitorEx<R> {

    R visit(@Nonnull EntityNameControlData entityNameControlData);

    R visit(@Nonnull FormData formData);

    R visit(@Nonnull GridControlData gridControlData);

    R visit(@Nonnull ImageControlData imageControlData);

    R visit(@Nonnull MultiChoiceControlData multiChoiceControlData);

    R visit(@Nonnull SingleChoiceControlData singleChoiceControlData);

    R visit(@Nonnull NumberControlData numberControlData);

    R visit(@Nonnull TextControlData textControlData);
}
