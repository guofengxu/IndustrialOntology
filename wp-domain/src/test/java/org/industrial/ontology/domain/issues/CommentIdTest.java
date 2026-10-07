package org.industrial.ontology.domain.issues;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.issues.CommentId_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommentIdTest {

    private CommentId commentId;

    @BeforeEach
    public void setUp() {
        commentId = CommentId.create();
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(commentId, is(commentId));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(commentId.equals(null), is(false));
    }

    @Test
    public void shouldNotBeEqualToOther() {
        assertThat(commentId, is(not(CommentId.create())));
    }

    @Test
    public void shouldNotBeEqualToOtherHashCode() {
        assertThat(commentId.hashCode(), is(not(CommentId.create().hashCode())));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(commentId.toString(), Matchers.startsWith("CommentId"));
    }
}
