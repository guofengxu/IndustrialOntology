package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FixedChoiceListSourceDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-11
 */
@JsonTypeName(FixedChoiceListSourceDescriptor.TYPE)
public record FixedChoiceListSourceDescriptor(@JsonProperty(FixedChoiceListSourceDescriptor.CHOICES) @Nonnull ImmutableList<ChoiceDescriptor> choices) implements ChoiceListSourceDescriptor {

    public FixedChoiceListSourceDescriptor {
        Objects.requireNonNull(choices, "Null choices");
    }

    public static final String TYPE = "Fixed";

    private static final String CHOICES = "choices";

    @JsonCreator
    public static FixedChoiceListSourceDescriptor get(@JsonProperty(CHOICES) @Nullable ImmutableList<ChoiceDescriptor> choices) {
        return new FixedChoiceListSourceDescriptor(choices == null ? ImmutableList.of() : choices);
    }

    @JsonProperty(CHOICES)
    @Nonnull
    public ImmutableList<ChoiceDescriptor> getChoices() {
        return choices;
    }
}
