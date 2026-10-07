package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.form.data.PrimitiveFormControlDataDto;
import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.ChoiceDescriptorDto}.
 */
public record ChoiceDescriptorDto(@Nonnull LanguageMap label, @Nonnull PrimitiveFormControlDataDto value) {

    public ChoiceDescriptorDto {
        Objects.requireNonNull(label, "Null label");
        Objects.requireNonNull(value, "Null value");
    }

    @Nonnull
    public static ChoiceDescriptorDto get(@Nonnull PrimitiveFormControlDataDto value, @Nonnull LanguageMap label) {
        return new ChoiceDescriptorDto(label, value);
    }

    @Nonnull
    public LanguageMap getLabel() {
        return label;
    }

    @Nonnull
    public PrimitiveFormControlDataDto getValue() {
        return value;
    }
}
