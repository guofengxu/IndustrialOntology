package org.industrial.ontology.domain.form.field;

import javax.annotation.Nonnull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionOrdering}.
 */
public record FormRegionOrdering(@JsonProperty(FormRegionOrdering.REGION_ID) @Nonnull @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT) @JsonSubTypes({ @JsonSubTypes.Type(value = FormFieldId.class), @JsonSubTypes.Type(value = GridColumnId.class) }) FormRegionId regionId, @JsonProperty(FormRegionOrdering.DIRECTION) @Nonnull FormRegionOrderingDirection direction) {

    public FormRegionOrdering {
        Objects.requireNonNull(regionId, "Null regionId");
        Objects.requireNonNull(direction, "Null direction");
    }

    public static final String REGION_ID = "regionId";

    public static final String DIRECTION = "direction";

    @JsonCreator
    @Nonnull
    public static FormRegionOrdering get(@JsonProperty(REGION_ID) @Nonnull FormRegionId formRegionId, @JsonProperty(DIRECTION) @Nonnull FormRegionOrderingDirection direction) {
        return new FormRegionOrdering(formRegionId, direction);
    }

    @JsonIgnore
    public boolean isAscending() {
        return getDirection().equals(FormRegionOrderingDirection.ASC);
    }

    @JsonProperty(REGION_ID)
    @Nonnull
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonSubTypes({ @JsonSubTypes.Type(value = FormFieldId.class), @JsonSubTypes.Type(value = GridColumnId.class) })
    public FormRegionId getRegionId() {
        return regionId;
    }

    @JsonProperty(DIRECTION)
    @Nonnull
    public FormRegionOrderingDirection getDirection() {
        return direction;
    }
}
