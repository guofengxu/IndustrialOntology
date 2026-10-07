package org.industrial.ontology.domain.form.field;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.form.data.LiteralFormControlData;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.MultiChoiceControlDescriptor_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-01-08
 */
public class MultiChoiceControlDescriptor_SerializationTest {

    private ImmutableList<ChoiceDescriptor> choices;

    @BeforeEach
    public void setUp() {
        choices = ImmutableList.of(ChoiceDescriptor.choice(LanguageMap.empty(), LiteralFormControlData.get(DataFactory.getOWLLiteral("A"))), ChoiceDescriptor.choice(LanguageMap.empty(), LiteralFormControlData.get(DataFactory.getOWLLiteral("B"))));
    }

    @Test
    public void shouldSerialize_AnnotationComponentCriteria() throws IOException {
        testSerialization(MultiChoiceControlDescriptor.get(FixedChoiceListSourceDescriptor.get(choices), ImmutableList.of()));
    }

    private static <V extends MultiChoiceControlDescriptor> void testSerialization(V value) throws IOException {
        JsonSerializationTestUtil.testSerialization(value, MultiChoiceControlDescriptor.class);
    }
}
