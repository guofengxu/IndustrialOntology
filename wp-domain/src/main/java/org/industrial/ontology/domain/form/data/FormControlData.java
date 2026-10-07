package org.industrial.ontology.domain.form.data;



import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.FormControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-30
 */
public interface FormControlData {

    <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor);

    void accept(@Nonnull FormControlDataVisitor visitor);
}
