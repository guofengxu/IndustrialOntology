package org.industrial.ontology.kernel.crud.obo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.oboid.OboIdSuffixSettings;
import org.industrial.ontology.domain.crud.oboid.UserIdRange;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OboIdSuffixSettings_SerializationTestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-07
 */
public class OboIdSuffixSettings_SerializationTest {

    public static final int TOTAL_DIGITS = 77;

    public static final int START = 100;

    public static final int END = 203;

    public static final UserId THE_USER = UserId.getUserId("TheUser");

    private OboIdSuffixSettings settings;

    @BeforeEach
    public void setUp() {
        settings = OboIdSuffixSettings.get(TOTAL_DIGITS, ImmutableList.of(UserIdRange.get(THE_USER, START, END)));
    }

    @Test
    public void shouldRoundTripSettings() throws IOException {
        JsonSerializationTestUtil.testSerialization(settings, EntityCrudKitSuffixSettings.class);
    }

    @Test
    public void shouldDeserializeLongClassNameForBackwardsCompatibility() throws IOException {
        var serialization = "{\"_class\":\"org.industrial.ontology.domain.crud.oboid.OBOIdSuffixSettings\",\"totalDigits\":77,\"userIdRanges\":[{\"userId\":\"TheUser\",\"start\":100,\"end\":203}]}";
        ObjectMapperProvider objectMapperProvider = new ObjectMapperProvider();
        ObjectMapper objectMapper = objectMapperProvider.get();
        var deserializedObject = objectMapper.readValue(serialization, EntityCrudKitSuffixSettings.class);
        assertThat(deserializedObject, is(settings));
    }
}
