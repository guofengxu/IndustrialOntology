package org.industrial.ontology.domain.form.field;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.form.data.LiteralFormControlData;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.industrial.ontology.domain.match.EntityTypeIsOneOfCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.ChoiceListSourceDescriptor_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-11
 */
public class ChoiceListSourceDescriptor_SerializationTest {

    ImmutableList<ChoiceDescriptor> choices;

    @BeforeEach
    public void setUp() {
        choices = ImmutableList.of(ChoiceDescriptor.choice(LanguageMap.empty(), LiteralFormControlData.get(DataFactory.getOWLLiteral("A"))), ChoiceDescriptor.choice(LanguageMap.empty(), LiteralFormControlData.get(DataFactory.getOWLLiteral("B"))));
    }

    @Test
    public void shouldSerialize_FixedList() throws IOException {
        testSerialization(FixedChoiceListSourceDescriptor.get(choices));
    }

    @Test
    public void shouldSerialize_DynamicList() throws IOException {
        testSerialization(DynamicChoiceListSourceDescriptor.get(EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.CLASS))));
    }

    private static <V extends ChoiceListSourceDescriptor> void testSerialization(V value) throws IOException {
        JsonSerializationTestUtil.testSerialization(value, ChoiceListSourceDescriptor.class);
    }
}
