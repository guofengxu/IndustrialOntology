package org.industrial.ontology.domain.form;



/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.FormSubjectFactoryDescriptorMissingException}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
public class FormSubjectFactoryDescriptorMissingException extends RuntimeException {

    public FormSubjectFactoryDescriptorMissingException() {
        super("Subject factory descriptor is missing.  Improperly configured form descriptor.");
    }
}
