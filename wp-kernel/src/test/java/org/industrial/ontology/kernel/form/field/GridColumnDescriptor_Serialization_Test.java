package org.industrial.ontology.kernel.form.field;

import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.UUID;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.form.field.Optionality;
import org.industrial.ontology.domain.form.field.Repeatability;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.field.GridColumnDescriptor_Serialization_Test}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-05-05
 */
public class GridColumnDescriptor_Serialization_Test {

    private GridColumnDescriptor descriptor;

    @BeforeEach
    public void setUp() {
        descriptor = GridColumnDescriptor.get(GridColumnId.get(UUID.randomUUID().toString()), Optionality.OPTIONAL, Repeatability.NON_REPEATABLE, null, LanguageMap.empty(), TextControlDescriptor.getDefault());
    }

    @Test
    public void shouldRoundTrip() throws IOException {
        JsonSerializationTestUtil.testSerialization(descriptor, GridColumnDescriptor.class);
    }
}
