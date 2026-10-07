package org.industrial.ontology.kernel.crud.supplied;



import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;
import org.industrial.ontology.domain.crud.supplied.WhiteSpaceTreatment;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.supplied.SuppliedNameSuffixSettings_SerializationTestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-07
 */
public class SuppliedNameSuffixSettings_SerializationTest {

    @Test
    public void shouldRoundTrip() throws IOException {
        var settings = SuppliedNameSuffixSettings.get(WhiteSpaceTreatment.REPLACE_WITH_DASHES);
        JsonSerializationTestUtil.testSerialization(settings, EntityCrudKitSuffixSettings.class);
    }

    @Test
    public void shouldDeserializeFullClassNameForBackwardsCompatibility() throws IOException {
        var serialization = "{\"_class\" : \"org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings\"}";
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
        ObjectMapper objectMapper = objectMapperProvider.get();
        var deserializedObject = objectMapper.readValue(serialization, EntityCrudKitSuffixSettings.class);
        assertThat(deserializedObject, is(SuppliedNameSuffixSettings.get()));
    }
}
