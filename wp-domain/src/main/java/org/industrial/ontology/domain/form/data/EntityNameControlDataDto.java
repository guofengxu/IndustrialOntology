package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.EntityNameControlDataDto}.
 */
public record EntityNameControlDataDto(int depth, @JsonProperty("descriptor") @Nonnull EntityNameControlDescriptor descriptor, @Nullable @JsonProperty("entity") OWLEntityData entityInternal) implements FormControlDataDto {

    public EntityNameControlDataDto {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    public static EntityNameControlDataDto get(@Nonnull EntityNameControlDescriptor descriptor, @Nonnull OWLEntityData entityData, int depth) {
        return new EntityNameControlDataDto(depth, descriptor, entityData);
    }

    @JsonIgnore
    @Nonnull
    public Optional<OWLEntityData> getEntity() {
        return Optional.ofNullable(getEntityInternal());
    }

    @Override
    public <R> R accept(FormControlDataDtoVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Nonnull
    @Override
    public FormControlData toFormControlData() {
        return EntityNameControlData.get(getDescriptor(), getEntity().map(OWLEntityData::getEntity).orElse(null));
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @JsonProperty("descriptor")
    @Nonnull
    public EntityNameControlDescriptor getDescriptor() {
        return descriptor;
    }

    @Nullable
    @JsonProperty("entity")
    public OWLEntityData getEntityInternal() {
        return entityInternal;
    }
}
