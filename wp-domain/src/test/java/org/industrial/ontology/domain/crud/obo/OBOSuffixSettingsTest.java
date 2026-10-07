package org.industrial.ontology.domain.crud.obo;



import org.industrial.ontology.domain.crud.oboid.OBOIdSuffixKit;
import org.industrial.ontology.domain.crud.oboid.OboIdSuffixSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.obo.OBOSuffixSettingsTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/08/2013
 */
public class OBOSuffixSettingsTest {

    @Test
    public void getKitIdReturnsTheCorrectId() {
        OboIdSuffixSettings settings = OboIdSuffixSettings.get();
        assertEquals(OBOIdSuffixKit.getId(), settings.getKitId());
    }

    @Test
    public void shouldSupplyDefaultTotalDigits() {
        OboIdSuffixSettings settings = OboIdSuffixSettings.get();
        assertEquals(7, settings.getTotalDigits());
    }
}
