package org.industrial.ontology.kernel.match;

import org.industrial.ontology.domain.match.StringEndsWithCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringEndsWithMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-06-23
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StringEndsWithMatcherTest {

    public static final boolean DO_NOT_IGNORE_CASE = false;

    public static final boolean IGNORE_CASE = true;

    private StringEndsWithMatcher matcher;

    @Mock
    private StringEndsWithCriteria criteria;

    @BeforeEach
    public void setUp() throws Exception {
        matcher = new StringEndsWithMatcher(criteria);
    }

    @Test
    public void shouldMatchStringEnd() {
        when(criteria.getValue()).thenReturn("world");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(true));
    }

    @Test
    public void shouldNotMatchStringEnd() {
        when(criteria.getValue()).thenReturn("World");
        when(criteria.isIgnoreCase()).thenReturn(DO_NOT_IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(false));
    }

    @Test
    public void shouldMatchStringEndIgnoringCase() {
        when(criteria.getValue()).thenReturn("world");
        when(criteria.isIgnoreCase()).thenReturn(IGNORE_CASE);
        var matches = matcher.matches("Hello world");
        assertThat(matches, is(true));
    }
}
