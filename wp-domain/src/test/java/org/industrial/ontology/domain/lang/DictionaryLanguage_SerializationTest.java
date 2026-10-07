package org.industrial.ontology.domain.lang;



import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.shortform.DictionaryLanguage_Serialization_TestCase}.
 */
public class DictionaryLanguage_SerializationTest {

    @Test
    public void shouldSerializeLocalNameDictionaryLanguage() throws IOException {
        var dictionaryLanguage = LocalNameDictionaryLanguage.get();
        JsonSerializationTestUtil.testSerialization(dictionaryLanguage, DictionaryLanguage.class);
    }

    @Test
    public void shouldSerializeOboIdDictionaryLanguage() throws IOException {
        var dictionaryLanguage = OboIdDictionaryLanguage.get();
        JsonSerializationTestUtil.testSerialization(dictionaryLanguage, DictionaryLanguage.class);
    }

    @Test
    public void shouldSerializeAnnotationBasedDictionaryLanguage() throws IOException {
        var dictionaryLanguage = AnnotationAssertionDictionaryLanguage.rdfsLabel("en");
        JsonSerializationTestUtil.testSerialization(dictionaryLanguage, DictionaryLanguage.class);
    }

    @Test
    public void shouldDeserializeLocalNameLegacySerialization() throws IOException {
        var localName = LocalNameDictionaryLanguage.get();
        JsonSerializationTestUtil.testDeserialization("{}", localName, DictionaryLanguage.class);
    }

    @Test
    public void shouldDeserializeAnnotationAssertionWithEmptyLanguageTagLegacySerialization() throws IOException {
        var iriString = "http://example.org/prop";
        var dictionaryLanguage = AnnotationAssertionDictionaryLanguage.get(iriString, "");
        var serialization = String.format("{\"propertyIri\":\"%s\"}", iriString);
        JsonSerializationTestUtil.testDeserialization(serialization, dictionaryLanguage, DictionaryLanguage.class);
    }

    @Test
    public void shouldDeserializeAnnotationAssertionWithNonEmptyLanguageTagLegacySerialization() throws IOException {
        var iriString = "http://example.org/prop";
        var dictionaryLanguage = AnnotationAssertionDictionaryLanguage.get(iriString, "en");
        var serialization = String.format("{\"propertyIri\":\"%s\", \"lang\":\"en\"}", iriString);
        JsonSerializationTestUtil.testDeserialization(serialization, dictionaryLanguage, DictionaryLanguage.class);
    }
}
