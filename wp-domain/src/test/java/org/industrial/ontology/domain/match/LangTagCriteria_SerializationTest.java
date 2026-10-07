package org.industrial.ontology.domain.match;



import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.industrial.ontology.domain.match.JsonSerializationTestUtil.testSerialization;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.LangTagCriteria_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public class LangTagCriteria_SerializationTest {


    @Test
    public void shouldSerialize_AnyLangTagOrEmptyLangTagCriteria() throws IOException {
        testSerialization(AnyLangTagOrEmptyLangTagCriteria.get(), LangTagCriteria.class);
    }

    @Test
    public void shouldSerialize_LangTagIsEmptyCriteria() throws IOException {
        testSerialization(LangTagIsEmptyCriteria.get(), LangTagCriteria.class);
    }

    @Test
    public void shouldSerialize_LangTagMatchesCriteria() throws IOException {
        testSerialization(LangTagMatchesCriteria.get("*-GB"), LangTagCriteria.class);
    }

}
