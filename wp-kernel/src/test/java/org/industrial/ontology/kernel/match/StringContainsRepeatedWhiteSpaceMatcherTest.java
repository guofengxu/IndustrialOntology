package org.industrial.ontology.kernel.match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.StringContainsRepeatedWhiteSpaceMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
public class StringContainsRepeatedWhiteSpaceMatcherTest {

    private StringContainsRepeatedWhiteSpaceMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new StringContainsRepeatedWhiteSpaceMatcher();
    }

    @Test
    public void shouldMatchRepeatedWhiteSpaceAtStart() {
        assertThat(matcher.matches("  A String"), is(true));
    }

    @Test
    public void shouldMatchRepeatedWhiteSpaceAtEnd() {
        assertThat(matcher.matches("A String  "), is(true));
    }

    @Test
    public void shouldMatchRepeatedWhiteInMiddle() {
        assertThat(matcher.matches("A  String"), is(true));
    }

    @Test
    public void shouldNotMatchString() {
        assertThat(matcher.matches("A String"), is(false));
    }
}
