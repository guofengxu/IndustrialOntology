package org.industrial.ontology.domain.form.field;



import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-22
 */
public interface FormRegionId {

    @JsonValue
    String getId();
}
