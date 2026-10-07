package org.industrial.ontology.domain.crud;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.EntityCrudKitSettings}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/08/2013
 */
public record EntityCrudKitSettings<S extends EntityCrudKitSuffixSettings>(@JsonProperty(EntityCrudKitSettings.PREFIX_SETTINGS) EntityCrudKitPrefixSettings prefixSettings, @JsonProperty(EntityCrudKitSettings.SUFFIX_SETTINGS) S suffixSettings) implements Serializable {

    public EntityCrudKitSettings {
        Objects.requireNonNull(prefixSettings, "Null prefixSettings");
        Objects.requireNonNull(suffixSettings, "Null suffixSettings");
    }

    public static final String PREFIX_SETTINGS = "prefixSettings";

    public static final String SUFFIX_SETTINGS = "suffixSettings";

    /**
     * Constructs an {@link EntityCrudKitSettings} object for the specified {@link EntityCrudKitPrefixSettings} and
     * {@link EntityCrudKitSuffixSettings}.
     * @param prefixSettings The {@link EntityCrudKitPrefixSettings}. Not {@code null}.
     * @param suffixSettings The {@link EntityCrudKitSuffixSettings}. Not {@code null}.
     * @throws NullPointerException if any argument is {@code null}.
     */
    @JsonCreator
    public static <S extends EntityCrudKitSuffixSettings> EntityCrudKitSettings<S> get(@JsonProperty(PREFIX_SETTINGS) @Nonnull EntityCrudKitPrefixSettings prefixSettings, @JsonProperty(SUFFIX_SETTINGS) S suffixSettings) {
        return new EntityCrudKitSettings<S>(prefixSettings, suffixSettings);
    }

    /**
     * Gets the prefix settings for this settings object.
     * @return The prefix settings.  Not {@code null}.
     */
    @JsonProperty(PREFIX_SETTINGS)
    public EntityCrudKitPrefixSettings getPrefixSettings() {
        return prefixSettings;
    }

    /**
     * Gets the suffix settings for this object.
     * @return The suffix settings. Not {@code null}.
     */
    @JsonProperty(SUFFIX_SETTINGS)
    public S getSuffixSettings() {
        return suffixSettings;
    }
}
