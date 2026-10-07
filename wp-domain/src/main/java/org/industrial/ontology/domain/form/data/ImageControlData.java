package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.semanticweb.owlapi.model.IRI;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.ImageControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record ImageControlData(@JsonProperty("descriptor") @Nonnull ImageControlDescriptor descriptor, @JsonProperty("iri") @Nullable IRI iriInternal) implements FormControlData {

    public ImageControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @JsonCreator
    public static ImageControlData get(@JsonProperty("descriptor") @Nonnull ImageControlDescriptor descriptor, @JsonProperty("iri") @Nullable IRI iri) {
        return new ImageControlData(descriptor, iri);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @Nonnull
    public Optional<IRI> getIri() {
        return Optional.ofNullable(getIriInternal());
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
