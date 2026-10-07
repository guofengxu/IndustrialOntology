package org.industrial.ontology.domain.lang;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.shortform.DictionaryLanguageFilter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-14
 */
public record DictionaryLanguageFilter(@Nonnull LangTagFilter langTagFilter, @Nonnull EmptyLangTagTreatment emptyLangTagTreatment) {

    public DictionaryLanguageFilter {
        Objects.requireNonNull(langTagFilter, "Null langTagFilter");
        Objects.requireNonNull(emptyLangTagTreatment, "Null emptyLangTagTreatment");
    }

    public enum EmptyLangTagTreatment {

        INCLUDE_EMPTY_LANG_TAGS, EXCLUDE_EMPTY_LANG_TAGS
    }

    @Nonnull
    public static DictionaryLanguageFilter get(@Nonnull LangTagFilter langTagFilter, @Nonnull EmptyLangTagTreatment emptyLangTagTreatment) {
        return new DictionaryLanguageFilter(langTagFilter, emptyLangTagTreatment);
    }

    /**
     * Determines whether the specified {@link DictionaryLanguage} is included by this filter.  This
     * depends upon the underlying {@link LangTagFilter} and the underlying {@link EmptyLangTagTreatment}.
     * If the specified {@link DictionaryLanguage} does not have a lang tag, or if the lang tag is empty
     * then it will be included by this filter if the value of {@link EmptyLangTagTreatment} is set
     * to {@link EmptyLangTagTreatment#INCLUDE_EMPTY_LANG_TAGS}, otherwise it will be excluded.  If the
     * specified {@link DictionaryLanguage} has a lang tag then it will be included if the lang tag is
     * included by the underlying {@link LangTagFilter}.
     * @param dictionaryLanguage The dictionaryLanguage.
     * @return true if this filter includes the specified language tag, otherwise false.
     */
    public boolean isIncluded(@Nonnull DictionaryLanguage dictionaryLanguage) {
        String lang = dictionaryLanguage.getLang();
        if (lang.isEmpty()) {
            return getEmptyLangTagTreatment().equals(EmptyLangTagTreatment.INCLUDE_EMPTY_LANG_TAGS);
        }
        return getLangTagFilter().isIncluded(lang);
    }

    @Nonnull
    public LangTagFilter getLangTagFilter() {
        return langTagFilter;
    }

    @Nonnull
    public EmptyLangTagTreatment getEmptyLangTagTreatment() {
        return emptyLangTagTreatment;
    }
}
