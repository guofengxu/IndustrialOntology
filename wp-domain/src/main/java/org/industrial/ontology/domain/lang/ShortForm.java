package org.industrial.ontology.domain.lang;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.shortform.ShortForm}.
 */
public record ShortForm(@JsonProperty(ShortForm.DICTIONARY_LANGUAGE) @Nonnull DictionaryLanguage dictionaryLanguage, @Nonnull @JsonProperty(ShortForm.SHORT_FORM) String shortForm) {

    public ShortForm {
        Objects.requireNonNull(dictionaryLanguage, "Null dictionaryLanguage");
        Objects.requireNonNull(shortForm, "Null shortForm");
    }

    public static final String DICTIONARY_LANGUAGE = "dictionaryLanguage";

    public static final String SHORT_FORM = "shortForm";

    @Nonnull
    public static ShortForm get(@Nonnull @JsonProperty(DICTIONARY_LANGUAGE) DictionaryLanguage language, @Nonnull @JsonProperty(SHORT_FORM) String shortForm) {
        return new ShortForm(language, shortForm);
    }

    @JsonCreator
    @Nonnull
    protected static ShortForm getFromJson(@Nullable @JsonProperty(DICTIONARY_LANGUAGE) DictionaryLanguage language, @Nonnull @JsonProperty(SHORT_FORM) String shortForm) {
        return new ShortForm(language == null ? LocalNameDictionaryLanguage.get() : language, shortForm);
    }

    @JsonProperty(DICTIONARY_LANGUAGE)
    @Nonnull
    public DictionaryLanguage getDictionaryLanguage() {
        return dictionaryLanguage;
    }

    @Nonnull
    @JsonProperty(SHORT_FORM)
    public String getShortForm() {
        return shortForm;
    }
}
