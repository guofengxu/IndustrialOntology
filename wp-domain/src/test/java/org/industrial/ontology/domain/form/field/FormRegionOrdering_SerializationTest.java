package org.industrial.ontology.domain.form.field;



import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormRegionOrdering_Serialization_TestCase}.
 */
public class FormRegionOrdering_SerializationTest {

    @Test
    public void shouldSerializeOrderBy() throws IOException {
        var orderBy = FormRegionOrdering.get(GridColumnId.get("12345678-1234-1234-1234-123456789abc"),
                                             FormRegionOrderingDirection.DESC);
        JsonSerializationTestUtil.testSerialization(orderBy, FormRegionOrdering.class);
    }
}
