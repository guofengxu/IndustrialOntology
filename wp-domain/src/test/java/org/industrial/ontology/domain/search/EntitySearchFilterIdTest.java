package org.industrial.ontology.domain.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.search.EntitySearchFilterId_TestCase}.
 */
public class EntitySearchFilterIdTest {

    public static final String ID = "12345678-1234-1234-1234-123456789abc";

    private EntitySearchFilterId filterId;

    @BeforeEach
    public void setUp() throws Exception {
        filterId = EntitySearchFilterId.get(ID);
    }

    @Test
    public void shouldReturnSuppliedUuid() {
        assertThat(filterId.getId(), is(ID));
    }

    /**
     * @noinspection ConstantConditions
     */
    @Test
    public void shouldThrowNPEForNullId() {
        assertThrows(NullPointerException.class, () -> {
            EntitySearchFilterId.get(null);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionForNonUuid() {
        assertThrows(IllegalArgumentException.class, () -> {
            EntitySearchFilterId.get("OtherId");
        });
    }
}
