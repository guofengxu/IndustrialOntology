package org.industrial.ontology.domain.form.field;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.GridColumnId_TestCase}.
 */
public class GridColumnIdTest {

    public static final String UUID = "12345678-1234-1234-1234-123456789abc";

    @Test
    public void shouldCreateId() {
        GridColumnId id = GridColumnId.get(UUID);
        assertThat(id.getId(), equalTo(UUID));
    }

    @Test
    public void shouldThrowExceptionForMalformedId() {
        assertThrows(IllegalArgumentException.class, () -> {
            GridColumnId.get("NotAUuid");
        });
    }
}
