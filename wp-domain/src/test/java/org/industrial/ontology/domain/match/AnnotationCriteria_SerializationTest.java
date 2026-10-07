package org.industrial.ontology.domain.match;



import org.junit.jupiter.api.Test;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.match.AnnotationCriteria_Serialization_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 18 Jun 2018
 */
public class AnnotationCriteria_SerializationTest {

    @Test
    public void shouldSerialize_AnnotationComponentCriteria() throws IOException {
        testSerialization(
                AnnotationComponentsCriteria.get(
                        AnyAnnotationPropertyCriteria.get(),
                        AnyAnnotationValueCriteria.get(),
                        AnyAnnotationSetCriteria.get()
                )
        );
    }

    private static <V extends AnnotationCriteria> void testSerialization(V value) throws IOException {
        JsonSerializationTestUtil.testSerialization(value, AnnotationCriteria.class);
    }
}
