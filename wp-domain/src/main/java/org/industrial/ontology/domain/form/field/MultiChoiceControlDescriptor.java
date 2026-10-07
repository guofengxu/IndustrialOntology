package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.MultiChoiceControlDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
@JsonTypeName(MultiChoiceControlDescriptor.TYPE)
public record MultiChoiceControlDescriptor(@Nonnull ChoiceListSourceDescriptor source, @Nonnull ImmutableList<ChoiceDescriptor> defaultChoices) implements FormControlDescriptor {

    public MultiChoiceControlDescriptor {
        Objects.requireNonNull(source, "Null source");
        Objects.requireNonNull(defaultChoices, "Null defaultChoices");
    }

    @Nonnull
    public static final String TYPE = "MULTI_CHOICE";

    @JsonCreator
    public static MultiChoiceControlDescriptor get(@JsonProperty("source") ChoiceListSourceDescriptor source, @JsonProperty("defaultChoices") @Nullable ImmutableList<ChoiceDescriptor> defaultChoices) {
        return new MultiChoiceControlDescriptor(source == null ? FixedChoiceListSourceDescriptor.get(ImmutableList.of()) : source, defaultChoices == null ? ImmutableList.of() : defaultChoices);
    }

    @JsonIgnore
    @Nonnull
    public static String getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public String getAssociatedType() {
        return TYPE;
    }

    @Override
    public <R> R accept(@Nonnull FormControlDescriptorVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    public ChoiceListSourceDescriptor getSource() {
        return source;
    }

    @Nonnull
    public ImmutableList<ChoiceDescriptor> getDefaultChoices() {
        return defaultChoices;
    }
}
