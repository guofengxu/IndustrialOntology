package org.industrial.ontology.domain.crud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.HasIRIPrefix;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitPrefixSettings}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2013
 * <p>
 * The legacy Jackson output put {@code conditionalIriPrefixes} first, and so does this record; the stored documents
 * of the {@code EntityCrudKitSettings} collection depend on it (docs/01 §5.3, sample
 * {@code legacy-mongo/EntityCrudKitSettings.json}).
 */
@JsonPropertyOrder({EntityCrudKitPrefixSettings.CONDITIONAL_IRI_PREFIXES, EntityCrudKitPrefixSettings.IRI_PREFIX})
public record EntityCrudKitPrefixSettings(@JsonProperty(EntityCrudKitPrefixSettings.IRI_PREFIX) String iriprefix, @JsonProperty(EntityCrudKitPrefixSettings.CONDITIONAL_IRI_PREFIXES) ImmutableList<ConditionalIriPrefix> conditionalIriPrefixes) implements HasIRIPrefix {

    public EntityCrudKitPrefixSettings {
        Objects.requireNonNull(iriprefix, "Null iriprefix");
        Objects.requireNonNull(conditionalIriPrefixes, "Null conditionalIriPrefixes");
    }

    public static final String DEFAULT_IRI_PREFIX = "http://www.example.org/";

    public static final String IRI_PREFIX = "iriPrefix";

    public static final String CONDITIONAL_IRI_PREFIXES = "conditionalIriPrefixes";

    @Nonnull
    public static EntityCrudKitPrefixSettings get() {
        return get(DEFAULT_IRI_PREFIX, ImmutableList.of());
    }

    @Nonnull
    public static EntityCrudKitPrefixSettings get(@Nonnull @JsonProperty(IRI_PREFIX) String iriPrefix, @Nonnull @JsonProperty(CONDITIONAL_IRI_PREFIXES) ImmutableList<ConditionalIriPrefix> conditionalIriPrefixes) {
        return new EntityCrudKitPrefixSettings(iriPrefix, conditionalIriPrefixes);
    }

    @JsonCreator
    @Nonnull
    protected static EntityCrudKitPrefixSettings create(@Nonnull @JsonProperty(IRI_PREFIX) String iriPrefix, @Nullable @JsonProperty(CONDITIONAL_IRI_PREFIXES) ImmutableList<ConditionalIriPrefix> conditionalIriPrefixes) {
        if (conditionalIriPrefixes == null) {
            return get(iriPrefix, ImmutableList.of());
        } else {
            return get(iriPrefix, conditionalIriPrefixes);
        }
    }

    @Override
    @JsonProperty(IRI_PREFIX)
    public String getIRIPrefix() {
        return iriprefix;
    }

    @JsonProperty(CONDITIONAL_IRI_PREFIXES)
    public ImmutableList<ConditionalIriPrefix> getConditionalIriPrefixes() {
        return conditionalIriPrefixes;
    }
}
