package org.industrial.ontology.domain.lang;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.LangTagFilter_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LangTagFilterTest {

    @Mock
    private LangTag langTag, otherLangTag;

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullSet() {
        assertThrows(NullPointerException.class, () -> {
            LangTagFilter.get(null);
        });
    }

    @Test
    public void shouldIncludeAnyLangTagForEmptySet() {
        LangTagFilter langTagFilter = LangTagFilter.get(ImmutableSet.of());
        assertThat(langTagFilter.isIncluded(langTag), is(true));
    }

    @Test
    public void shouldIncludeAnyExplicitLangTag() {
        LangTagFilter langTagFilter = LangTagFilter.get(ImmutableSet.of(langTag));
        assertThat(langTagFilter.isIncluded(langTag), is(true));
    }

    @Test
    public void shouldNotIncludeAnyNonExplicityLangTagInNonEmptySet() {
        LangTagFilter langTagFilter = LangTagFilter.get(ImmutableSet.of(langTag));
        assertThat(langTagFilter.isIncluded(otherLangTag), is(false));
    }
}
