package org.industrial.ontology.domain.form.field;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.ImageControlDescriptorDto}.
 */
public record ImageControlDescriptorDto() implements FormControlDescriptorDto {

    @Nonnull
    public static ImageControlDescriptorDto get() {
        return new ImageControlDescriptorDto();
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ImageControlDescriptor toFormControlDescriptor() {
        return new ImageControlDescriptor();
    }
}
