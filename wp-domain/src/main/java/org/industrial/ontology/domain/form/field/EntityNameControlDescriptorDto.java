package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.EntityNameControlDescriptorDto}.
 */
public record EntityNameControlDescriptorDto(@Nonnull LanguageMap placeholder, @JsonIgnore @Nullable CompositeRootCriteria matchCriteriaInternal) implements FormControlDescriptorDto {

    public EntityNameControlDescriptorDto {
        Objects.requireNonNull(placeholder, "Null placeholder");
    }

    @Nonnull
    public static EntityNameControlDescriptorDto get(@Nonnull LanguageMap placeholder, @Nullable CompositeRootCriteria matchCriteria) {
        return new EntityNameControlDescriptorDto(placeholder, matchCriteria);
    }

    @Override
    public <R> R accept(FormControlDescriptorDtoVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public EntityNameControlDescriptor toFormControlDescriptor() {
        return EntityNameControlDescriptor.get(getPlaceholder(), getMatchCriteriaInternal());
    }

    @Nonnull
    public Optional<CompositeRootCriteria> getMatchCriteria() {
        return Optional.ofNullable(getMatchCriteriaInternal());
    }

    @Nonnull
    public LanguageMap getPlaceholder() {
        return placeholder;
    }

    @JsonIgnore
    @Nullable
    public CompositeRootCriteria getMatchCriteriaInternal() {
        return matchCriteriaInternal;
    }
}
