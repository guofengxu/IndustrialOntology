package org.industrial.ontology.domain.perspective;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveId_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PerspectiveIdTest {

    private PerspectiveId perspectiveId;

    private String id = "12345678-1234-1234-1234-123456789abc";

    @BeforeEach
    public void setUp() throws Exception {
        perspectiveId = PerspectiveId.get(id);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_id_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            PerspectiveId.get(null);
        });
    }

    @Test
    public void shouldReturnSupplied_id() {
        assertThat(perspectiveId.getId(), is(this.id));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(perspectiveId, is(perspectiveId));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(perspectiveId.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(perspectiveId, is(PerspectiveId.get(id)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_id() {
        assertThat(perspectiveId, is(not(PerspectiveId.get("4c96af5c-31d0-4b5a-9d57-ed48e6668da4"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(perspectiveId.hashCode(), is(PerspectiveId.get(id).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(perspectiveId.toString(), startsWith("PerspectiveId"));
    }
}
