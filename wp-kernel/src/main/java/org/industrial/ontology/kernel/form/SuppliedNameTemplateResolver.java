package org.industrial.ontology.kernel.form;



import org.semanticweb.owlapi.model.EntityType;

import java.util.UUID;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.SuppliedNameTemplateResolver}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-12
 */
public class SuppliedNameTemplateResolver {

    public SuppliedNameTemplateResolver() {
    }

    public String resolveTemplateVariables(String suppliedNameTemplate,
                                           EntityType<?> entityType) {
        return suppliedNameTemplate
                .replace("${uuid}", UUID.randomUUID().toString())
                .replace("${type}", entityType.getPrintName());

    }
}
