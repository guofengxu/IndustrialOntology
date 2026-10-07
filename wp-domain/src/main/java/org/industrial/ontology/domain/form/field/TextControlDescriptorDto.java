package org.industrial.ontology.domain.form.field;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.TextControlDescriptorDto}.
 */
public record TextControlDescriptorDto(@Nonnull TextControlDescriptor descriptor) implements FormControlDescriptorDto {

    public TextControlDescriptorDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    public static TextControlDescriptorDto get(@Nonnull TextControlDescriptor descriptor) {
        return new TextControlDescriptorDto(descriptor);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public FormControlDescriptor toFormControlDescriptor() {
        return getDescriptor();
    }

    @Nonnull
    public TextControlDescriptor getDescriptor() {
        return descriptor;
    }
}
