package org.industrial.ontology.kernel.match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.regex.Pattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringContainsRegexMatchMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
public class StringContainsRegexMatchMatcherTest {

    private StringContainsRegexMatchMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new StringContainsRegexMatchMatcher(Pattern.compile("[A-Z]"));
    }

    @Test
    public void shouldMatchValue() {
        assertThat(matcher.matches("A"), is(true));
    }

    @Test
    public void shouldNotMatchValue() {
        assertThat(matcher.matches("0"), is(false));
    }
}
