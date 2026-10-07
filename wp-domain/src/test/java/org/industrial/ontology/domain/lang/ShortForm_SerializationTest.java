package org.industrial.ontology.domain.lang;



import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.shortform.ShortForm_Serialization_TestCase}.
 */
public class ShortForm_SerializationTest {

    @Test
    public void shouldSerializeLocalNameShortForm() throws IOException {
        var shortForm = ShortForm.get(
                DictionaryLanguage.localName(),
                "Hello"
        );
        JsonSerializationTestUtil.testSerialization(shortForm, ShortForm.class);
    }

    @Test
    public void shouldSerializeAnnotationBasedShortFormWithEmptyLangTag() throws IOException {
        var shortForm = ShortForm.get(
                DictionaryLanguage.rdfsLabel(""),
                "Hello"
        );
        JsonSerializationTestUtil.testSerialization(shortForm, ShortForm.class);
    }

    @Test
    public void shouldSerializeAnnotationBasedShortFormWithNonEmptyLangTag() throws IOException {
        var shortForm = ShortForm.get(
                DictionaryLanguage.rdfsLabel("en"),
                "Hello"
        );
        JsonSerializationTestUtil.testSerialization(shortForm, ShortForm.class);
    }
}
