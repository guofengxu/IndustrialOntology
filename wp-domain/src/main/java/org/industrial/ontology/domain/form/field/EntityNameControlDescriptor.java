package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.semanticweb.owlapi.model.EntityType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import org.industrial.ontology.domain.match.EntityTypeIsOneOfCriteria;
import org.industrial.ontology.domain.match.MultiMatchType;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.EntityNameControlDescriptor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
@JsonTypeName(EntityNameControlDescriptor.TYPE)
public record EntityNameControlDescriptor(@Nonnull LanguageMap placeholder, @JsonIgnore @Nullable CompositeRootCriteria matchCriteriaInternal) implements FormControlDescriptor {

    public EntityNameControlDescriptor {
        Objects.requireNonNull(placeholder, "Null placeholder");
    }

    protected static final String TYPE = "ENTITY_NAME";

    @JsonCreator
    @Nonnull
    public static EntityNameControlDescriptor get(@Nullable @JsonProperty("placeholder") LanguageMap languageMap, @Nullable @JsonProperty("matchCriteria") CompositeRootCriteria criteria) {
        return new EntityNameControlDescriptor(languageMap == null ? LanguageMap.empty() : languageMap, criteria);
    }

    @Nonnull
    public static EntityNameControlDescriptor getDefault() {
        return new EntityNameControlDescriptor(LanguageMap.empty(), getDefaultEntityMatchCriteria());
    }

    public static CompositeRootCriteria getDefaultEntityMatchCriteria() {
        return CompositeRootCriteria.get(ImmutableList.of(EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.CLASS))), MultiMatchType.ALL);
    }

    public static String getFieldTypeId() {
        return TYPE;
    }

    @Nonnull
    @Override
    @JsonIgnore
    public String getAssociatedType() {
        return TYPE;
    }

    @Override
    public <R> R accept(@Nonnull FormControlDescriptorVisitor<R> visitor) {
        return visitor.visit(this);
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
