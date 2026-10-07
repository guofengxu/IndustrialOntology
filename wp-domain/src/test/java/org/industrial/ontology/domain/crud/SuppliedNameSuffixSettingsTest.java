package org.industrial.ontology.domain.crud;



import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;
import org.industrial.ontology.domain.crud.supplied.WhiteSpaceTreatment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.SuppliedNameSuffixSettingsTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class SuppliedNameSuffixSettingsTest {

    @Test
    public void shouldReturnDefaultValueForWhiteSpaceTreatment() {
        SuppliedNameSuffixSettings settings = SuppliedNameSuffixSettings.get();
        assertEquals(settings.getWhiteSpaceTreatment(), WhiteSpaceTreatment.TRANSFORM_TO_CAMEL_CASE);
    }

    @Test
    public void shouldReturnSuppliedWhiteSpaceTreatment() {
        SuppliedNameSuffixSettings settings = SuppliedNameSuffixSettings.get(WhiteSpaceTreatment.ESCAPE);
        assertEquals(settings.getWhiteSpaceTreatment(), WhiteSpaceTreatment.ESCAPE);
    }
}
