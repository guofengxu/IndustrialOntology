package org.industrial.ontology.domain.lang;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.DisplayNameSettings}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 31 Jul 2018
 */
public record DisplayNameSettings(@JsonProperty(DisplayNameSettings.PRIMARY_DISPLAY_NAME_LANGUAGES) @Nonnull ImmutableList<DictionaryLanguage> primaryDisplayNameLanguages, @JsonProperty(DisplayNameSettings.SECONDARY_DISPLAY_NAME_LANGUAGES) @Nonnull ImmutableList<DictionaryLanguage> secondaryDisplayNameLanguages) {

    public DisplayNameSettings {
        Objects.requireNonNull(primaryDisplayNameLanguages, "Null primaryDisplayNameLanguages");
        Objects.requireNonNull(secondaryDisplayNameLanguages, "Null secondaryDisplayNameLanguages");
    }

    private static final String PRIMARY_DISPLAY_NAME_LANGUAGES = "primaryDisplayNameLanguages";

    private static final String SECONDARY_DISPLAY_NAME_LANGUAGES = "secondaryDisplayNameLanguages";

    @JsonCreator
    @Nonnull
    public static DisplayNameSettings get(@Nullable @JsonProperty(PRIMARY_DISPLAY_NAME_LANGUAGES) ImmutableList<DictionaryLanguage> primaryLanguages, @Nullable @JsonProperty(SECONDARY_DISPLAY_NAME_LANGUAGES) ImmutableList<DictionaryLanguage> secondaryLanguages) {
        return new DisplayNameSettings(primaryLanguages == null ? ImmutableList.of() : primaryLanguages, secondaryLanguages == null ? ImmutableList.of() : secondaryLanguages);
    }

    @Nonnull
    public static DisplayNameSettings empty() {
        return get(ImmutableList.of(), ImmutableList.of());
    }

    public boolean hasDisplayNameLanguageForLangTag(@Nonnull String langTag) {
        Stream<DictionaryLanguage> languages = Streams.concat(getPrimaryDisplayNameLanguages().stream(), getSecondaryDisplayNameLanguages().stream());
        return languages.anyMatch(l -> langTag.equalsIgnoreCase(l.getLang()));
    }

    @JsonProperty(PRIMARY_DISPLAY_NAME_LANGUAGES)
    @Nonnull
    public ImmutableList<DictionaryLanguage> getPrimaryDisplayNameLanguages() {
        return primaryDisplayNameLanguages;
    }

    @JsonProperty(SECONDARY_DISPLAY_NAME_LANGUAGES)
    @Nonnull
    public ImmutableList<DictionaryLanguage> getSecondaryDisplayNameLanguages() {
        return secondaryDisplayNameLanguages;
    }
}
