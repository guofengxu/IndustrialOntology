package org.industrial.ontology.domain.form.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.data.EntityNameControlData}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public record EntityNameControlData(@Nonnull EntityNameControlDescriptor descriptor, @Nullable @JsonProperty("entity") OWLEntity entityInternal) implements FormControlData {

    public EntityNameControlData {
        Objects.requireNonNull(descriptor, "Null descriptor");
    }

    @JsonCreator
    public static EntityNameControlData get(@JsonProperty("descriptor") @Nonnull EntityNameControlDescriptor descriptor, @Nullable OWLEntity entity) {
        return new EntityNameControlData(descriptor, entity);
    }

    @Override
    public <R> R accept(@Nonnull FormControlDataVisitorEx<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void accept(@Nonnull FormControlDataVisitor visitor) {
        visitor.visit(this);
    }

    @JsonIgnore
    @Nonnull
    public Optional<OWLEntity> getEntity() {
        return Optional.ofNullable(getEntityInternal());
    }

    @Nonnull
    public EntityNameControlDescriptor getDescriptor() {
        return descriptor;
    }

    @Nullable
    @JsonProperty("entity")
    public OWLEntity getEntityInternal() {
        return entityInternal;
    }
}
