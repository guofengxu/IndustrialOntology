package org.industrial.ontology.domain.form.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.ChoiceDescriptor_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-11-09
 */
public class ChoiceDescriptor_SerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapperProvider().get();
    }

    @Test
    public void shouldSerializeAndDeserializeChoiceDescriptor() throws IOException {
        var label = LanguageMap.of("en", "Hello World");
        var value = PrimitiveFormControlData.get(new OWLClassImpl(IRI.create("http://example.org/A")));
        var choiceDescriptor = ChoiceDescriptor.choice(label, value);
        var serialized = objectMapper.writeValueAsString(choiceDescriptor);
        System.out.println(serialized);
        var deserialized = objectMapper.readerFor(ChoiceDescriptor.class).readValue(serialized);
        assertThat(deserialized, Matchers.is(choiceDescriptor));
    }
}
