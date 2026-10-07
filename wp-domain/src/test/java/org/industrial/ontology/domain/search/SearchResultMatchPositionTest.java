package org.industrial.ontology.domain.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.SearchResultMatchPosition_TestCase}.
 */
public class SearchResultMatchPositionTest {

    private SearchResultMatchPosition position;

    private int start = 3;

    private int end = 5;

    @BeforeEach
    public void setUp() throws Exception {
        position = SearchResultMatchPosition.get(start, end);
    }

    @Test
    public void shouldGetPositionWithSuppliedStart() {
        assertThat(position.getStart(), is(start));
    }

    @Test
    public void shouldGetPositionWithSuppliedEnd() {
        assertThat(position.getEnd(), is(end));
    }

    @Test
    public void compareToUsingDifferentStartPosition() {
        SearchResultMatchPosition otherPositionWithSmallerStart = SearchResultMatchPosition.get(start - 1, end);
        assertThat(position, is(greaterThan(otherPositionWithSmallerStart)));
    }

    @Test
    public void shouldCompareToUsingDifferentEndPosition() {
        SearchResultMatchPosition otherPositionWithSameStartSmallerEnd = SearchResultMatchPosition.get(start, end - 5);
        assertThat(position, is(greaterThan(otherPositionWithSameStartSmallerEnd)));
    }

    @Test
    public void shouldCompareEqual() {
        SearchResultMatchPosition equalPosition = SearchResultMatchPosition.get(start, end);
        assertThat(position, is(greaterThanOrEqualTo(equalPosition)));
        assertThat(position, is(lessThanOrEqualTo(equalPosition)));
    }
}
