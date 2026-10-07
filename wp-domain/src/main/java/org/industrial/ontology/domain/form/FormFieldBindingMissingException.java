package org.industrial.ontology.domain.form;



import org.industrial.ontology.domain.form.field.FormFieldId;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormFieldBindingMissingException}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class FormFieldBindingMissingException extends RuntimeException {

    private FormFieldId formFieldId;

    public FormFieldBindingMissingException(@Nonnull FormFieldId formFieldId) {
        super("Form field binding is missing for " + formFieldId.getId() + ".  Improperly configured form.");
        this.formFieldId = checkNotNull(formFieldId);
    }

    private FormFieldBindingMissingException() {
    }

    public FormFieldId getFormFieldId() {
        return formFieldId;
    }
}
