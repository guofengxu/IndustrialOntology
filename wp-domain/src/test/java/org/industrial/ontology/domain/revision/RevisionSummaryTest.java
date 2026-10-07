package org.industrial.ontology.domain.revision;

import org.industrial.ontology.domain.core.UserId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.revision.RevisionSummary_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RevisionSummaryTest {

    private RevisionSummary revisionSummary;

    @Mock
    private RevisionNumber revisionNumber;

    @Mock
    private UserId userId;

    private long timestamp = 1L;

    private int changeCount = 1;

    private String description = "The description";

    @BeforeEach
    public void setUp() throws Exception {
        revisionSummary = new RevisionSummary(revisionNumber, userId, timestamp, changeCount, description);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_revisionNumber_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new RevisionSummary(null, userId, timestamp, changeCount, description);
        });
    }

    @Test
    public void shouldReturnSupplied_revisionNumber() {
        assertThat(revisionSummary.getRevisionNumber(), is(this.revisionNumber));
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_usedId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new RevisionSummary(revisionNumber, null, timestamp, changeCount, description);
        });
    }

    @Test
    public void shouldReturnSupplied_timestamp() {
        assertThat(revisionSummary.getTimestamp(), is(this.timestamp));
    }

    @Test
    public void shouldReturnSupplied_changeCount() {
        assertThat(revisionSummary.getChangeCount(), is(this.changeCount));
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_description_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new RevisionSummary(revisionNumber, userId, timestamp, changeCount, null);
        });
    }

    @Test
    public void shouldReturnSupplied_description() {
        assertThat(revisionSummary.getDescription(), is(this.description));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(revisionSummary, is(revisionSummary));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(revisionSummary.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(revisionSummary, is(new RevisionSummary(revisionNumber, userId, timestamp, changeCount, description)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_revisionNumber() {
        assertThat(revisionSummary, is(not(new RevisionSummary(mock(RevisionNumber.class), userId, timestamp, changeCount, description))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_usedId() {
        assertThat(revisionSummary, is(not(new RevisionSummary(revisionNumber, mock(UserId.class), timestamp, changeCount, description))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_timestamp() {
        assertThat(revisionSummary, is(not(new RevisionSummary(revisionNumber, userId, 2L, changeCount, description))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_changeCount() {
        assertThat(revisionSummary, is(not(new RevisionSummary(revisionNumber, userId, timestamp, 2, description))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_description() {
        assertThat(revisionSummary, is(not(new RevisionSummary(revisionNumber, userId, timestamp, changeCount, "String-8e6bda86-c114-4708-9f41-c19bb9d5d9c1"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(revisionSummary.hashCode(), is(new RevisionSummary(revisionNumber, userId, timestamp, changeCount, description).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(revisionSummary.toString(), Matchers.startsWith("RevisionSummary"));
    }

    @Test
    public void should_getUserId() {
        assertThat(revisionSummary.getUserId(), is(userId));
    }
}
