package org.industrial.ontology.domain.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.industrial.ontology.domain.lang.DictionaryLanguageFilter.EmptyLangTagTreatment.EXCLUDE_EMPTY_LANG_TAGS;
import static org.industrial.ontology.domain.lang.DictionaryLanguageFilter.EmptyLangTagTreatment.INCLUDE_EMPTY_LANG_TAGS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.shortform.DictionaryLanguageFilter_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DictionaryLanguageFilterTest {

    public static final String LANG_TAG = "en";

    public static final String EMPTY_LANG_TAG = "";

    @Mock
    private LangTagFilter langTagFilter;

    @Mock
    private DictionaryLanguage dictionaryLanguage;

    @BeforeEach
    public void setUp() throws Exception {
    }

    @Test
    public void shouldIncludeEmptyLangTag() {
        when(dictionaryLanguage.getLang()).thenReturn(EMPTY_LANG_TAG);
        DictionaryLanguageFilter filter = DictionaryLanguageFilter.get(langTagFilter, INCLUDE_EMPTY_LANG_TAGS);
        assertThat(filter.isIncluded(dictionaryLanguage), is(true));
    }

    @Test
    public void shouldNotIncludeEmptyLangTag() {
        when(dictionaryLanguage.getLang()).thenReturn(EMPTY_LANG_TAG);
        DictionaryLanguageFilter filter = DictionaryLanguageFilter.get(langTagFilter, EXCLUDE_EMPTY_LANG_TAGS);
        assertThat(filter.isIncluded(dictionaryLanguage), is(false));
    }

    @Test
    public void shouldIncludeNonEmptyLangTag() {
        when(dictionaryLanguage.getLang()).thenReturn(LANG_TAG);
        when(langTagFilter.isIncluded(LANG_TAG)).thenReturn(true);
        DictionaryLanguageFilter filter = DictionaryLanguageFilter.get(langTagFilter, EXCLUDE_EMPTY_LANG_TAGS);
        assertThat(filter.isIncluded(dictionaryLanguage), is(true));
    }

    @Test
    public void shouldNotIncludeNonEmptyLangTag() {
        when(dictionaryLanguage.getLang()).thenReturn(LANG_TAG);
        when(langTagFilter.isIncluded(LANG_TAG)).thenReturn(false);
        DictionaryLanguageFilter filter = DictionaryLanguageFilter.get(langTagFilter, EXCLUDE_EMPTY_LANG_TAGS);
        assertThat(filter.isIncluded(dictionaryLanguage), is(false));
    }
}
