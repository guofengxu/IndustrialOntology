package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.semanticweb.owlapi.model.OWLLiteral;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.NumberControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record NumberControlData(@Nonnull NumberControlDescriptor descriptor, @JsonProperty("value") @Nullable OWLLiteral valueInternal) implements FormControlData {

    public NumberControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @JsonCreator
    public static NumberControlData get(@Nonnull NumberControlDescriptor descriptor, @Nullable OWLLiteral value) {
        return new NumberControlData(descriptor, value);
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
    @JsonIgnore
    public Optional<OWLLiteral> getValue() {
        return Optional.ofNullable(getValueInternal());
    }

    @Nonnull
    public NumberControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("value")
    @Nullable
    public OWLLiteral getValueInternal() {
        return valueInternal;
    }
}
