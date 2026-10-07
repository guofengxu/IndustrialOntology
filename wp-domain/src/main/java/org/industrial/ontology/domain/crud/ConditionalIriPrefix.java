package org.industrial.ontology.domain.crud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.industrial.ontology.domain.match.CompositeHierarchyPositionCriteria;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.ConditionalIriPrefix}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-07
 */
public record ConditionalIriPrefix(@Nonnull String iriPrefix, @JsonProperty(ConditionalIriPrefix.CRITERIA) @Nonnull CompositeHierarchyPositionCriteria criteria) {

    public ConditionalIriPrefix {
        Objects.requireNonNull(iriPrefix, "Null iriPrefix");
        Objects.requireNonNull(criteria, "Null criteria");
    }

    public static final String IRI_PREFIX = "iriPrefix";

    public static final String CRITERIA = "criteria";

    public static ConditionalIriPrefix get() {
        return get(EntityCrudKitPrefixSettings.DEFAULT_IRI_PREFIX, CompositeHierarchyPositionCriteria.get());
    }

    @Nonnull
    public static ConditionalIriPrefix get(@Nonnull @JsonProperty(IRI_PREFIX) String iriPrefix, @Nonnull @JsonProperty(CRITERIA) CompositeHierarchyPositionCriteria criteria) {
        return new ConditionalIriPrefix(iriPrefix, criteria);
    }

    @Nonnull
    @JsonCreator
    protected static ConditionalIriPrefix create(@Nonnull @JsonProperty(IRI_PREFIX) String iriPrefix, @Nullable @JsonProperty(CRITERIA) CompositeHierarchyPositionCriteria criteria) {
        if (criteria == null) {
            return new ConditionalIriPrefix(iriPrefix, CompositeHierarchyPositionCriteria.get());
        } else {
            return new ConditionalIriPrefix(iriPrefix, criteria);
        }
    }

    @Nonnull
    public String getIriPrefix() {
        return iriPrefix;
    }

    @JsonProperty(CRITERIA)
    @Nonnull
    public CompositeHierarchyPositionCriteria getCriteria() {
        return criteria;
    }
}
