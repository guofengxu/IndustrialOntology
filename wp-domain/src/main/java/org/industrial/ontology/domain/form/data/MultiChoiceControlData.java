package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.MultiChoiceControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record MultiChoiceControlData(@JsonProperty("descriptor") @Nonnull MultiChoiceControlDescriptor descriptor, @JsonProperty("values") @Nonnull ImmutableList<PrimitiveFormControlData> values) implements FormControlData {

    public MultiChoiceControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
        Objects.requireNonNull(values, "Null values");
    }

    public static MultiChoiceControlData get(@JsonProperty("descriptor") @Nonnull MultiChoiceControlDescriptor descriptor, @JsonProperty("values") @Nonnull ImmutableList<PrimitiveFormControlData> values) {
        return new MultiChoiceControlData(descriptor, values);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @JsonProperty("descriptor")
    @Nonnull
    public MultiChoiceControlDescriptor getDescriptor() {
        return descriptor;
    }

    @JsonProperty("values")
    @Nonnull
    public ImmutableList<PrimitiveFormControlData> getValues() {
        return values;
    }
}
