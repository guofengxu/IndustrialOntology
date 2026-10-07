package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.lang.LanguageMap;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.NumberControlDescriptorDto}.
 */
public record NumberControlDescriptorDto(@Nonnull NumberControlDescriptor descriptor) implements FormControlDescriptorDto {

    public NumberControlDescriptorDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @Nonnull
    public static NumberControlDescriptorDto get(@Nonnull NumberControlDescriptor descriptor) {
        return new NumberControlDescriptorDto(descriptor);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public FormControlDescriptor toFormControlDescriptor() {
        return getDescriptor();
    }

    public String getFormat() {
        return getDescriptor().getFormat();
    }

    public NumberControlRange getRange() {
        return getDescriptor().getRange();
    }

    public LanguageMap getPlaceholder() {
        return getDescriptor().getPlaceholder();
    }

    public int getLength() {
        return getDescriptor().getLength();
    }

    @Nonnull
    public NumberControlDescriptor getDescriptor() {
        return descriptor;
    }
}
