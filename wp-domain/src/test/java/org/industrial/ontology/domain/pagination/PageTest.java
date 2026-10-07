package org.industrial.ontology.domain.pagination;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.pagination.PageTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 13/09/2013
 */
public class PageTest {

    @Test
    public void constructorThrowsIllegalArgumentExceptionIfPageNumberIsGreaterThanPageCount() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Page<String>(2, 1, Collections.emptyList(), 100);
        });
    }

    @Test
    public void constructorThrowsIndexOfOutBoundsExceptionForPageNumberOfZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Page<String>(0, 1, Collections.emptyList(), 100);
        });
    }

    @Test
    public void constructorThrowsIllegalArgumentExceptionForPageCountOfZero() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Page<String>(1, 0, Collections.emptyList(), 100);
        });
    }

    @Test
    public void constructorThrowsNullPointerExceptionForNullElements() {
        assertThrows(NullPointerException.class, () -> {
            new Page<String>(1, 1, null, 100);
        });
    }

    @Test
    public void getElementsReturnsCopy() {
        List<String> suppliedElements = Arrays.asList("A");
        Page<String> p = new Page(1, 1, suppliedElements, 100);
        List<String> elements = p.getPageElements();
        elements.clear();
        assertEquals(suppliedElements, p.getPageElements());
    }

    @Test
    public void getPageNumberReturnsSuppliedPageNumber() {
        Page<String> p = new Page<String>(2, 2, Collections.emptyList(), 100);
        assertEquals(2, p.getPageNumber());
    }
}
