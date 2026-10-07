package org.industrial.ontology.domain.form.field;

import org.industrial.ontology.domain.form.FormDescriptorDto;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.SubFormControlDescriptorDto}.
 */
public record SubFormControlDescriptorDto(@Nonnull FormDescriptorDto descriptor) implements FormControlDescriptorDto {

    public SubFormControlDescriptorDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @Nonnull
    public static SubFormControlDescriptorDto get(@Nonnull FormDescriptorDto subformDescriptorDto) {
        return new SubFormControlDescriptorDto(subformDescriptorDto);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public FormControlDescriptor toFormControlDescriptor() {
        return new SubFormControlDescriptor(getDescriptor().toFormDescriptor());
    }

    @Nonnull
    public FormDescriptorDto getDescriptor() {
        return descriptor;
    }
}
