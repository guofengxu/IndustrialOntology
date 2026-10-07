package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.ImageControlDataDto}.
 */
public record ImageControlDataDto(int depth, @JsonProperty("descriptor") @Nonnull ImageControlDescriptor descriptor, @JsonProperty("iri") @Nullable IRI iriInternal) implements FormControlDataDto {

    public ImageControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @Nonnull
    public static ImageControlDataDto get(@Nonnull ImageControlDescriptor descriptor, @Nonnull IRI iri, int depth) {
        return new ImageControlDataDto(depth, descriptor, iri);
    }

    @Nonnull
    public Optional<IRI> getIri() {
        return Optional.ofNullable(getIriInternal());
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public FormControlData toFormControlData() {
        return ImageControlData.get(getDescriptor(), getIriInternal());
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @JsonProperty("descriptor")
    @Nonnull
    public ImageControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("iri")
    @Nullable
    public IRI getIriInternal() {
        return iriInternal;
    }
}
