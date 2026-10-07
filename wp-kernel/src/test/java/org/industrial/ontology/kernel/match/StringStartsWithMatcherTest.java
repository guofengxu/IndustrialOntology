package org.industrial.ontology.kernel.match;

import org.industrial.ontology.domain.match.StringStartsWithCriteria;
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
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringStartsWithMatcher_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StringStartsWithMatcherTest {

    public static final boolean DO_NOT_IGNORE_CASE = false;

    public static final boolean IGNORE_CASE = true;

    private StringStartsWithMatcher matcher;

    @Mock
    private StringStartsWithCriteria criteria;

    @BeforeEach
    public void setUp() throws Exception {
        matcher = new StringStartsWithMatcher(criteria);
    }

    @Test
    public void shouldMatchStringStart() {
        when(criteria.getValue()).thenReturn("Hello");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(true));
    }

    @Test
    public void shouldNotMatchStringStart() {
        when(criteria.getValue()).thenReturn("hello");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(false));
    }

    @Test
    public void shouldMatchStringStartIgnoringCase() {
        when(criteria.getValue()).thenReturn("hello");
        when(criteria.isIgnoreCase()).thenReturn(IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(true));
    }
}
