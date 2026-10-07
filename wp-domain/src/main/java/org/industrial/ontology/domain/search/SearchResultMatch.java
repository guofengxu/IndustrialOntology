package org.industrial.ontology.domain.search;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.SearchResultMatch}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-17
 */
public record SearchResultMatch(@JsonProperty(SearchResultMatch.ENTITY) @Nonnull EntityNode entity, @JsonProperty(SearchResultMatch.LANGUAGE) @Nonnull DictionaryLanguage language, @JsonProperty(SearchResultMatch.LANGUAGE_RENDERING) @Nonnull ImmutableMap<DictionaryLanguage, String> languageRendering, @JsonProperty(SearchResultMatch.VALUE) @Nonnull String value, @JsonProperty(SearchResultMatch.POSITIONS) @Nonnull ImmutableList<SearchResultMatchPosition> positions) {

    public SearchResultMatch {
        Objects.requireNonNull(entity, "Null entity");
        Objects.requireNonNull(language, "Null language");
        Objects.requireNonNull(languageRendering, "Null languageRendering");
        Objects.requireNonNull(value, "Null value");
        Objects.requireNonNull(positions, "Null positions");
    }

    public static final String ENTITY = "entity";

    public static final String LANGUAGE = "language";

    private static final String LANGUAGE_RENDERING = "languageRendering";

    public static final String VALUE = "value";

    public static final String POSITIONS = "positions";

    @JsonCreator
    public static SearchResultMatch get(@JsonProperty(ENTITY) @Nonnull EntityNode entity, @JsonProperty(LANGUAGE) @Nonnull DictionaryLanguage matchedDictionaryLanguage, @JsonProperty(LANGUAGE_RENDERING) @Nonnull ImmutableMap<DictionaryLanguage, String> languageRendering, @JsonProperty(VALUE) @Nonnull String matchedString, @JsonProperty(POSITIONS) @Nonnull ImmutableList<SearchResultMatchPosition> searchResultMatchPositions) {
        return new SearchResultMatch(entity, matchedDictionaryLanguage, languageRendering, matchedString, searchResultMatchPositions);
    }

    @JsonProperty(ENTITY)
    @Nonnull
    public EntityNode getEntity() {
        return entity;
    }

    @JsonProperty(LANGUAGE)
    @Nonnull
    public DictionaryLanguage getLanguage() {
        return language;
    }

    @JsonProperty(LANGUAGE_RENDERING)
    @Nonnull
    public ImmutableMap<DictionaryLanguage, String> getLanguageRendering() {
        return languageRendering;
    }

    @JsonProperty(VALUE)
    @Nonnull
    public String getValue() {
        return value;
    }

    @JsonProperty(POSITIONS)
    @Nonnull
    public ImmutableList<SearchResultMatchPosition> getPositions() {
        return positions;
    }
}
