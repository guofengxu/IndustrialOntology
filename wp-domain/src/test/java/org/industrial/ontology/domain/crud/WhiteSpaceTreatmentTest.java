package org.industrial.ontology.domain.crud;



import org.industrial.ontology.domain.crud.supplied.WhiteSpaceTreatment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.crud.WhiteSpaceTreatmentTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/18/13
 */
public class WhiteSpaceTreatmentTest {

    @Test
    public void replaceWithUnderscoresTrimsWhiteSpace() {
        assertEquals("propertyName", WhiteSpaceTreatment.REPLACE_WITH_UNDERSCORES.transform(" propertyName "));
    }

    @Test
    public void replaceWithUnderscoresDoesReplace() {
        assertEquals("property_Name", WhiteSpaceTreatment.REPLACE_WITH_UNDERSCORES.transform("property Name"));
    }

    @Test
    public void replaceWithUnderscoresCollapsesAndDoesReplace() {
        assertEquals("property_Name", WhiteSpaceTreatment.REPLACE_WITH_UNDERSCORES.transform("property  Name"));
    }

    @Test
    public void replaceWithDashesTrimsWhiteSpace() {
        assertEquals("propertyName", WhiteSpaceTreatment.REPLACE_WITH_DASHES.transform(" propertyName "));
    }

    @Test
    public void replaceWithDashesDoesReplace() {
        assertEquals("property-Name", WhiteSpaceTreatment.REPLACE_WITH_DASHES.transform("property Name"));
    }

    @Test
    public void replaceWithDashesCollapsesAndDoesReplace() {
        assertEquals("property-Name", WhiteSpaceTreatment.REPLACE_WITH_DASHES.transform("property  Name"));
    }

    @Test
    public void transformsToCamelCaseTrimsWhiteSpace() {
        assertEquals("propertyName", WhiteSpaceTreatment.TRANSFORM_TO_CAMEL_CASE.transform(" propertyName "));
    }

    @Test
    public void transformToCamelCaseRemovesWhiteSpaceAndCapitalises() {
        assertEquals("ClassName", WhiteSpaceTreatment.TRANSFORM_TO_CAMEL_CASE.transform("Class name"));
    }

    @Test
    public void transformToCamelCaseRemovesMultipleWhiteSpaceAndCapitalises() {
        assertEquals("ClassName", WhiteSpaceTreatment.TRANSFORM_TO_CAMEL_CASE.transform("Class    name"));
    }

}
