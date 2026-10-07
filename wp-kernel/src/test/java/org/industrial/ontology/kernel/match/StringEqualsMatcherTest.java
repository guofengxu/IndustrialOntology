package org.industrial.ontology.kernel.match;

import org.industrial.ontology.domain.match.StringEqualsCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringEqualsMatcher_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StringEqualsMatcherTest {

    public static final boolean DO_NOT_IGNORE_CASE = false;

    public static final boolean IGNORE_CASE = true;

    private StringEqualsMatcher matcher;

    @Mock
    private StringEqualsCriteria criteria;

    @BeforeEach
    public void setUp() throws Exception {
        matcher = new StringEqualsMatcher(criteria);
    }

    @Test
    public void shouldMatch() {
        when(criteria.getValue()).thenReturn("Hello World");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello World");
        assertThat(matches, is(true));
    }

    @Test
    public void shouldNotMatch() {
        when(criteria.getValue()).thenReturn("hello world");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello World");
        assertThat(matches, is(false));
    }

    @Test
    public void shouldMatchIgnoringCase() {
        when(criteria.getValue()).thenReturn("hello world");
        when(criteria.isIgnoreCase()).thenReturn(IGNORE_CASE);
        var matches = matcher.matches("Hello World");
        assertThat(matches, is(true));
    }
}
