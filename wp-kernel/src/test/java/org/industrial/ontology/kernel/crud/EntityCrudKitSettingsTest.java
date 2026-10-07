package org.industrial.ontology.kernel.crud;

import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudKitSettingsTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/08/2013
 */
public class EntityCrudKitSettingsTest {

    @Test
    public void constructorThrowsNullPointerExceptionIfPrefixSettingsIsNull() {
        assertThrows(NullPointerException.class, () -> {
            EntityCrudKitSettings.get(null, mock(EntityCrudKitSuffixSettings.class));
        });
    }

    @Test
    public void constructorThrowsNullPointerExceptionIfSuffixSettingsIsNull() {
        assertThrows(NullPointerException.class, () -> {
            EntityCrudKitSettings.get(EntityCrudKitPrefixSettings.get(), null);
        });
    }

    @Test
    public void objectsWithSamePrefixAndSuffixSettingsHaveEqualHashCodes() {
        EntityCrudKitPrefixSettings prefixSettings = EntityCrudKitPrefixSettings.get();
        EntityCrudKitSuffixSettings suffixSettings = mock(EntityCrudKitSuffixSettings.class);
        EntityCrudKitSettings<?> settingsA = EntityCrudKitSettings.get(prefixSettings, suffixSettings);
        EntityCrudKitSettings<?> settingsB = EntityCrudKitSettings.get(prefixSettings, suffixSettings);
        assertEquals(settingsA.hashCode(), settingsB.hashCode());
    }

    @Test
    public void objectsWithSamePrefixAndSuffixSettingsAreEqual() {
        EntityCrudKitPrefixSettings prefixSettings = EntityCrudKitPrefixSettings.get();
        EntityCrudKitSuffixSettings suffixSettings = mock(EntityCrudKitSuffixSettings.class);
        EntityCrudKitSettings<?> settingsA = EntityCrudKitSettings.get(prefixSettings, suffixSettings);
        EntityCrudKitSettings<?> settingsB = EntityCrudKitSettings.get(prefixSettings, suffixSettings);
        assertEquals(settingsA, settingsB);
    }
}
