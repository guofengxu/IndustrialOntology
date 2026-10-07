package org.industrial.ontology.kernel.api.shortform;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ShortFormMatchPosition_TestCase}.
 */
public class ShortFormMatchPositionTest {

    @Test
    public void shouldCompareLessThanOnStart() {
        var p1 = ShortFormMatchPosition.get(1, 4);
        var p2 = ShortFormMatchPosition.get(2, 4);
        assertThat(p1.compareTo(p2), lessThan(0));
    }

    @Test
    public void shouldCompareLessThanOnEnd() {
        var p1 = ShortFormMatchPosition.get(1, 3);
        var p2 = ShortFormMatchPosition.get(1, 4);
        assertThat(p1.compareTo(p2), lessThan(0));
    }

    @Test
    public void shouldCompareEqual() {
        var p1 = ShortFormMatchPosition.get(1, 3);
        var p2 = ShortFormMatchPosition.get(1, 3);
        assertThat(p1.compareTo(p2), equalTo(0));
    }

    @Test
    public void shouldThrowExceptionForStartGreaterThanEnd() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShortFormMatchPosition.get(3, 2);
        });
    }

    @Test
    public void shouldThrowExceptionForStartLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            ShortFormMatchPosition.get(-2, 2);
        });
    }
}
