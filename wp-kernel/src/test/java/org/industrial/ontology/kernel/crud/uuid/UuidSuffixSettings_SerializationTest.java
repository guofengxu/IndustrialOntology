package org.industrial.ontology.kernel.crud.uuid;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidSuffixSettings_SerializationTestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-06
 */
public class UuidSuffixSettings_SerializationTest {

    @Test
    public void shouldDeserializeLegacySerialization() throws IOException {
        var serialization = "{\"_class\" : \"org.industrial.ontology.domain.crud.uuid.UUIDSuffixSettings\"}";
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
        ObjectMapper objectMapper = objectMapperProvider.get();
        var deserializedObject = objectMapper.readValue(serialization, EntityCrudKitSuffixSettings.class);
        assertThat(deserializedObject, is(UuidSuffixSettings.get()));
    }

    @Test
    public void shouldSerializeDefaultSettings() throws IOException {
        var settings = UuidSuffixSettings.get();
        JsonSerializationTestUtil.testSerialization(settings, EntityCrudKitSuffixSettings.class);
    }
}
