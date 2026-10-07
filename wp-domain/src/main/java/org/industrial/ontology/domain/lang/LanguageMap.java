package org.industrial.ontology.domain.lang;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LanguageMap}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-02
 */
public record LanguageMap(@Nonnull ImmutableMap<String, String> asMap) {

    public LanguageMap {
        Objects.requireNonNull(asMap, "Null asMap");
    }

    private static final LanguageMap EMPTY = get(ImmutableMap.of());

    public static LanguageMap of(String lang, String value) {
        return get(ImmutableMap.of(lang, value));
    }

    @JsonCreator
    @Nonnull
    public static LanguageMap get(Map<String, String> langMap) {
        return new LanguageMap(ImmutableMap.copyOf(langMap));
    }

    @JsonIgnore
    public static LanguageMap empty() {
        return EMPTY;
    }

    @JsonIgnore
    @Nonnull
    public static LanguageMapBuilder builder() {
        return new LanguageMapBuilder();
    }

    @Nonnull
    @JsonIgnore
    public String get(@Nonnull String langTag) {
        String normalizedLangTag = LanguageTagFormatter.format(langTag);
        ImmutableMap<String, String> map = asMap();
        String val = map.get(normalizedLangTag);
        if (val != null) {
            return val;
        }
        String superLang = normalizedLangTag;
        int lastIndexOfHyphen = superLang.lastIndexOf("-");
        while (lastIndexOfHyphen != -1) {
            superLang = superLang.substring(0, lastIndexOfHyphen);
            String superLangValue = map.get(superLang);
            if (superLangValue != null) {
                return superLangValue;
            }
            lastIndexOfHyphen = superLang.lastIndexOf("-");
        }
        return map.values().stream().findFirst().orElse("");
    }

    public static class LanguageMapBuilder {

        private final ImmutableMap.Builder<String, String> map = ImmutableMap.builder();

        public LanguageMap build() {
            return LanguageMap.get(map.build());
        }

        public LanguageMapBuilder put(String lang, String value) {
            String formattedLang = LanguageTagFormatter.format(lang);
            map.put(formattedLang, value);
            return this;
        }
    }

    @Nonnull
    public static LanguageMap fromDictionaryMap(@Nonnull Map<DictionaryLanguage, String> dictionaryMap) {
        Map<String, String> langMap = new HashMap<>();
        dictionaryMap.forEach((dict, value) -> {
            dict.accept(new DictionaryLanguageVisitor<String>() {

                @Override
                public String visit(@Nonnull AnnotationAssertionDictionaryLanguage language) {
                    langMap.putIfAbsent(language.getLang(), value);
                    return null;
                }

                @Override
                public String visit(@Nonnull AnnotationAssertionPathDictionaryLanguage language) {
                    langMap.putIfAbsent(language.getLang(), value);
                    return null;
                }

                @Override
                public String visit(@Nonnull LocalNameDictionaryLanguage language) {
                    langMap.putIfAbsent("", value);
                    return null;
                }
            });
        });
        return LanguageMap.get(ImmutableMap.copyOf(langMap));
    }

    @JsonValue
    @Nonnull
    public ImmutableMap<String, String> asMap() {
        return asMap;
    }
}
