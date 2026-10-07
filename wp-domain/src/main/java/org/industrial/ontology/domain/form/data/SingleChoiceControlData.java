package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.SingleChoiceControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record SingleChoiceControlData(@Nonnull SingleChoiceControlDescriptor descriptor, @JsonProperty("choice") @Nullable PrimitiveFormControlData choiceInternal) implements FormControlData {

    public SingleChoiceControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @JsonCreator
    public static SingleChoiceControlData get(@JsonProperty("descriptor") @Nonnull SingleChoiceControlDescriptor descriptor, @JsonProperty("choice") @Nullable PrimitiveFormControlData choice) {
        return new SingleChoiceControlData(descriptor, choice);
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
    public Optional<PrimitiveFormControlData> getChoice() {
        return Optional.ofNullable(getChoiceInternal());
    }

    @Nonnull
    public SingleChoiceControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("choice")
    @Nullable
    public PrimitiveFormControlData getChoiceInternal() {
        return choiceInternal;
    }
}
