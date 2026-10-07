package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.SingleChoiceControlDataDto}.
 */
public record SingleChoiceControlDataDto(int depth, @Nonnull SingleChoiceControlDescriptor descriptor, @JsonProperty("choice") @Nullable PrimitiveFormControlDataDto choiceInternal) implements FormControlDataDto {

    public SingleChoiceControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @Nonnull
    public static SingleChoiceControlDataDto get(@Nonnull SingleChoiceControlDescriptor descriptor, @Nullable PrimitiveFormControlDataDto choice, int depth) {
        return new SingleChoiceControlDataDto(depth, descriptor, choice);
    }

    @Nonnull
    public Optional<PrimitiveFormControlDataDto> getChoice() {
        return Optional.ofNullable(getChoiceInternal());
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public SingleChoiceControlData toFormControlData() {
        return SingleChoiceControlData.get(getDescriptor(), getChoice().map(PrimitiveFormControlDataDto::toPrimitiveFormControlData).orElse(null));
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Nonnull
    public SingleChoiceControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("choice")
    @Nullable
    public PrimitiveFormControlDataDto getChoiceInternal() {
        return choiceInternal;
    }
}
