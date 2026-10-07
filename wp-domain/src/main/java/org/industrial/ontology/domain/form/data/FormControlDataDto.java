package org.industrial.ontology.domain.form.data;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormControlDataDto}.
 */
public interface FormControlDataDto {

    <R> R accept(FormControlDataDtoVisitorEx<R> visitor);

    @Nonnull
    FormControlData toFormControlData();

    int getDepth();
}
