package org.industrial.ontology.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.EntityNameUtils_IsQuoted_TestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/11/2013
 */
public class EntityNameUtils_IsQuotedTest {

    @Test
    public void shouldThrowNullPointerExceptionIfStringIsNull() {
        assertThrows(NullPointerException.class, () -> {
            EntityNameUtils.isQuoted(null);
        });
    }

    @Test
    public void shouldReturnFalseForEmptyString() {
        boolean quoted = EntityNameUtils.isQuoted("");
        assertEquals(false, quoted);
    }

    @Test
    public void shouldReturnFalseForSingleSingleQuote() {
        boolean quoted = EntityNameUtils.isQuoted("'");
        assertEquals(false, quoted);
    }

    @Test
    public void shouldReturnFalseForSingleQuoteOnlyAtStart() {
        boolean quoted = EntityNameUtils.isQuoted("'a");
        assertEquals(false, quoted);
    }

    @Test
    public void shouldReturnFalseForSingleQuoteOnlyAtEnd() {
        boolean quoted = EntityNameUtils.isQuoted("a'");
        assertEquals(false, quoted);
    }

    @Test
    public void shouldReturnTrueForEmptyStringSurroundedBySingleQuotes() {
        boolean quoted = EntityNameUtils.isQuoted("''");
        assertEquals(true, quoted);
    }

    @Test
    public void shouldReturnTrueForSingleQuoteAtStartAndAtEnd() {
        boolean quoted = EntityNameUtils.isQuoted("'a'");
        assertEquals(true, quoted);
    }
}
