package org.industrial.ontology.domain.form.field;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.NumberControlType}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 25 Jun 2017
 */
public enum NumberControlType {

    @JsonProperty("Plain")
    PLAIN,

    @JsonProperty("Stepper")
    STEPPER,

    @JsonProperty("Slider")
    SLIDER
}
