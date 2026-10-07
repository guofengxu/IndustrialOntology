package org.industrial.ontology.domain.form.field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionOrdering_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormRegionOrderingTest {

    @Mock
    private GridColumnId columnId;

    private FormRegionOrderingDirection direction = FormRegionOrderingDirection.ASC;

    private FormRegionOrdering orderBy;

    @BeforeEach
    public void setUp() throws Exception {
        orderBy = FormRegionOrdering.get(columnId, direction);
    }

    @Test
    public void shouldReturnSuppliedColumnId() {
        assertThat(orderBy.getRegionId(), equalTo(columnId));
    }

    @Test
    public void shouldReturnSuppliedDirection() {
        assertThat(orderBy.getDirection(), equalTo(direction));
    }

    @Test
    public void shouldReturnTrueForAsc() {
        assertThat(orderBy.isAscending(), equalTo(true));
    }

    @Test
    public void shouldReturnFalseForDesc() {
        FormRegionOrdering orderByDesc = FormRegionOrdering.get(columnId, FormRegionOrderingDirection.DESC);
        assertThat(orderByDesc.isAscending(), equalTo(false));
    }
}
