package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.TextControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record TextControlData(@JsonProperty("descriptor") @Nonnull TextControlDescriptor descriptor, @JsonProperty("value") @Nullable OWLLiteral valueInternal) implements FormControlData {

    public TextControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @Nonnull
    public static TextControlData get(@JsonProperty("descriptor") @Nonnull TextControlDescriptor descriptor, @JsonProperty("value") @Nullable OWLLiteral value) {
        return new TextControlData(descriptor, value);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @JsonIgnore
    @Nonnull
    public Optional<OWLLiteral> getValue() {
        return Optional.ofNullable(getValueInternal());
    }

    @JsonProperty("descriptor")
    @Nonnull
    public TextControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("value")
    @Nullable
    public OWLLiteral getValueInternal() {
        return valueInternal;
    }
}
