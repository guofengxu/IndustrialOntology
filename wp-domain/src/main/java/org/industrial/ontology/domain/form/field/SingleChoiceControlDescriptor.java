package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.SingleChoiceControlDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
@JsonTypeName(SingleChoiceControlDescriptor.TYPE)
public record SingleChoiceControlDescriptor(@JsonProperty(SingleChoiceControlDescriptor.WIDGET_TYPE) @Nonnull SingleChoiceControlType widgetType, @JsonProperty(SingleChoiceControlDescriptor.SOURCE) @Nonnull ChoiceListSourceDescriptor source, @JsonProperty(SingleChoiceControlDescriptor.DEFAULT_CHOICE) @Nullable ChoiceDescriptor defaultChoiceInternal) implements FormControlDescriptor {

    public SingleChoiceControlDescriptor {
        Objects.requireNonNull(widgetType, "Null widgetType");
        Objects.requireNonNull(source, "Null source");
    }

    protected static final String TYPE = "SINGLE_CHOICE";

    private static final String DEFAULT_CHOICE = "defaultChoice";

    private static final String SOURCE = "source";

    private static final String WIDGET_TYPE = "widgetType";

    @JsonCreator
    protected static SingleChoiceControlDescriptor get(@JsonProperty(WIDGET_TYPE) @Nullable SingleChoiceControlType widgetType, @JsonProperty(DEFAULT_CHOICE) @Nullable ChoiceDescriptor defaultChoice, @JsonProperty(SOURCE) @Nullable ChoiceListSourceDescriptor source) {
        return new SingleChoiceControlDescriptor(widgetType == null ? SingleChoiceControlType.COMBO_BOX : widgetType, source == null ? FixedChoiceListSourceDescriptor.get(ImmutableList.of()) : source, defaultChoice);
    }

    @Nonnull
    public static SingleChoiceControlDescriptor get(@Nonnull SingleChoiceControlType widgetType, @Nonnull ChoiceListSourceDescriptor source, @Nonnull ChoiceDescriptor defaultChoice) {
        return new SingleChoiceControlDescriptor(widgetType, source, defaultChoice);
    }

    @Nonnull
    public static SingleChoiceControlDescriptor get(@Nonnull SingleChoiceControlType widgetType, @Nonnull ChoiceListSourceDescriptor source) {
        return new SingleChoiceControlDescriptor(widgetType, source, null);
    }

    @Nonnull
    public static String getType() {
        return TYPE;
    }

    @Nonnull
    @Override
    public String getAssociatedType() {
        return TYPE;
    }

    @JsonIgnore
    @Nonnull
    public Optional<ChoiceDescriptor> getDefaultChoice() {
        return Optional.ofNullable(getDefaultChoiceInternal());
    }

    @Override
    public <R> R accept(@Nonnull FormControlDescriptorVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @JsonProperty(WIDGET_TYPE)
    @Nonnull
    public SingleChoiceControlType getWidgetType() {
        return widgetType;
    }

    @JsonProperty(SOURCE)
    @Nonnull
    public ChoiceListSourceDescriptor getSource() {
        return source;
    }

    @JsonProperty(DEFAULT_CHOICE)
    @Nullable
    public ChoiceDescriptor getDefaultChoiceInternal() {
        return defaultChoiceInternal;
    }
}
