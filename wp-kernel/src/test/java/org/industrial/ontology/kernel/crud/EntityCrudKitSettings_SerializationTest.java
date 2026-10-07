package org.industrial.ontology.kernel.crud;

import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.match.JsonSerializationTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudKitSettings_SerializationTestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-07
 */
public class EntityCrudKitSettings_SerializationTest {

    private EntityCrudKitSettings<UuidSuffixSettings> settings;

    @BeforeEach
    public void setUp() {
        settings = EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(), UuidSuffixSettings.get());
    }

    @Test
    public void shouldRoundTrip() throws IOException {
        JsonSerializationTestUtil.testSerialization(settings, EntityCrudKitSettings.class);
    }
}
