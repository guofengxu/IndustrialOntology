package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.ChoiceDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
public record ChoiceDescriptor(@Nonnull LanguageMap label, @Nonnull PrimitiveFormControlData value) {

    public ChoiceDescriptor {
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(value, "Null value");
    }

    @JsonCreator
    public static ChoiceDescriptor choice(@Nonnull @JsonProperty("label") LanguageMap label, @Nonnull @JsonProperty("value") PrimitiveFormControlData value) {
        return new ChoiceDescriptor(label, value);
    }

    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @Nonnull
    public PrimitiveFormControlData getValue() {
        return value;
    }
}
