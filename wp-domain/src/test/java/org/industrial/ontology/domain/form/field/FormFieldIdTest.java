package org.industrial.ontology.domain.form.field;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.form.field.FormFieldId_TestCase}.
 */
public class FormFieldIdTest {

    public static final String UUID = "12345678-1234-1234-1234-123456789abc";

    @Test
    public void shouldGetFormFieldIdWithSuppliedUUID() {
        FormFieldId id = FormFieldId.get(UUID);
        assertThat(id.getId(), equalTo(UUID));
    }

    @Test
    public void shouldNotAcceptMalformedId() {
        assertThrows(IllegalArgumentException.class, () -> {
            FormFieldId.get("NotAUUID");
        });
    }

    @Test
    public void shouldNotAcceptNull() {
        assertThrows(NullPointerException.class, () -> {
            FormFieldId.get(null);
        });
    }
}
