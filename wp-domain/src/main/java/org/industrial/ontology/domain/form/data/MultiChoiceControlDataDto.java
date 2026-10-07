package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import javax.annotation.Nonnull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.MultiChoiceControlDataDto}.
 */
public record MultiChoiceControlDataDto(int depth, @JsonProperty("descriptor") @Nonnull MultiChoiceControlDescriptor descriptor, @JsonProperty("values") @Nonnull ImmutableList<PrimitiveFormControlDataDto> values) implements FormControlDataDto {

    public MultiChoiceControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
        Objects.requireNonNull(values, "Null values");
    }

    @Nonnull
    public static MultiChoiceControlDataDto get(@Nonnull MultiChoiceControlDescriptor descriptor, @Nonnull ImmutableList<PrimitiveFormControlDataDto> values, int depth) {
        return new MultiChoiceControlDataDto(depth, descriptor, values);
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public MultiChoiceControlData toFormControlData() {
        return MultiChoiceControlData.get(getDescriptor(), getValues().stream().map(PrimitiveFormControlDataDto::toPrimitiveFormControlData).collect(toImmutableList()));
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @JsonProperty("descriptor")
    @Nonnull
    public MultiChoiceControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("values")
    @Nonnull
    public ImmutableList<PrimitiveFormControlDataDto> getValues() {
        return values;
    }
}
