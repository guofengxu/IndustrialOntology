package org.industrial.ontology.domain.hierarchy;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.hierarchy.HierarchyId_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HierarchyIdTest {

    private static final String ID = "TheHierarchy";

    private HierarchyId hierarchyId;

    @BeforeEach
    public void setUp() {
        hierarchyId = HierarchyId.get(ID);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            HierarchyId.get(null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(hierarchyId, is(hierarchyId));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(hierarchyId.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(hierarchyId, is(HierarchyId.get(ID)));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(hierarchyId.hashCode(), is(HierarchyId.get(ID).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(hierarchyId.toString(), Matchers.startsWith("HierarchyId"));
    }
}
