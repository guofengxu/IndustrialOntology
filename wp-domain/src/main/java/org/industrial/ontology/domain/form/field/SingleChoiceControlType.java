package org.industrial.ontology.domain.form.field;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.SingleChoiceControlType}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
public enum SingleChoiceControlType {

    @JsonProperty("RadioButton")
    RADIO_BUTTON,

    @JsonProperty("ComboBox")
    COMBO_BOX,

    @JsonProperty("SegmentedButton")
    SEGMENTED_BUTTON
}
