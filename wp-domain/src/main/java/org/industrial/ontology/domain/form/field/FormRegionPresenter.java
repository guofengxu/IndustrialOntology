package org.industrial.ontology.domain.form.field;




import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionPresenter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-23
 */
public interface FormRegionPresenter {

    @Nonnull
    FormRegionId getFormRegionId();
}
