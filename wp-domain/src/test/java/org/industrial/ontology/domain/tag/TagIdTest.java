package org.industrial.ontology.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.TagId_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TagIdTest {

    private static final String THE_ID = "12345678-1234-1234-1234-123456789abc";

    private TagId tagId;

    @BeforeEach
    public void setUp() {
        tagId = TagId.getId(THE_ID);
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(tagId, is(tagId));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(tagId.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(tagId, is(TagId.getId(THE_ID)));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(tagId.hashCode(), is(TagId.getId(THE_ID).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(tagId.toString(), startsWith("TagId"));
    }

    @Test
    public void should_getId() {
        assertThat(tagId.getId(), is(THE_ID));
    }
}
