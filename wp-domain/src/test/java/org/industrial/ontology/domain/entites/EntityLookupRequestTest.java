package org.industrial.ontology.domain.entites;

import org.industrial.ontology.domain.entity.EntityLookupRequest;
import org.industrial.ontology.domain.search.SearchType;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entites.EntityLookupRequestTestCase}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 12/11/2013
 */
public class EntityLookupRequestTest {

    @Test
    public void nullSearchStringThrowsNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest(null);
        });
    }

    @Test
    public void nullSearchStringThrowsNullPointerException2() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest(null, SearchType.SUB_STRING_MATCH_IGNORE_CASE);
        });
    }

    @Test
    public void nullSearchStringThrowsNullPointerException3() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest(null, SearchType.SUB_STRING_MATCH_IGNORE_CASE, 20, Arrays.asList(EntityType.CLASS), null);
        });
    }

    @Test
    public void nullSearchTypeThrowsNullPointerException2() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest("Test", null);
        });
    }

    @Test
    public void nullSearchTypeThrowsNullPointerException3() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest("Test", null, 20, Arrays.asList(EntityType.CLASS), null);
        });
    }

    @Test
    public void negativeSearchLimitThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, -1, Arrays.asList(EntityType.CLASS), null);
        });
    }

    @Test
    public void nullSearchedEntityTypesThrowsNullPointerExeception() {
        assertThrows(NullPointerException.class, () -> {
            new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 1, null, null);
        });
    }

    @Test
    public void defaultSearchTypeIsTheSameAsSearchTypeDefault() {
        EntityLookupRequest request = new EntityLookupRequest("Test");
        assertEquals(SearchType.getDefault(), request.getSearchType());
    }

    @Test
    public void suppliedSearchedEntityTypesIsCopied() {
        Set<EntityType<?>> types = new HashSet<EntityType<?>>();
        types.add(EntityType.CLASS);
        Set<EntityType<?>> typesCopy = new HashSet<EntityType<?>>(types);
        EntityLookupRequest request = new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 20, types, null);
        types.add(EntityType.OBJECT_PROPERTY);
        assertEquals(typesCopy, request.getSearchedEntityTypes());
    }

    @Test
    public void getSearchStringReturnsSuppliedString() {
        EntityLookupRequest request = new EntityLookupRequest("Test");
        assertEquals("Test", request.getSearchString());
    }

    @Test
    public void getSearchLimitReturnsSuppliedSearchLimit() {
        EntityLookupRequest request = new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 5, Arrays.asList(EntityType.CLASS), null);
        assertEquals(5, request.getSearchLimit());
    }

    @Test
    public void getSearchTypeReturnsSuppliedType() {
        EntityLookupRequest request = new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 5, Arrays.asList(EntityType.CLASS), null);
        assertEquals(SearchType.SUB_STRING_MATCH_IGNORE_CASE, request.getSearchType());
    }

    @Test
    public void equalArgsGiveEqualRequests() {
        EntityLookupRequest requestA = new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 5, Arrays.asList(EntityType.CLASS), null);
        EntityLookupRequest requestB = new EntityLookupRequest("Test", SearchType.SUB_STRING_MATCH_IGNORE_CASE, 5, Arrays.asList(EntityType.CLASS), null);
        assertEquals(requestA.hashCode(), requestB.hashCode());
        assertEquals(requestA, requestB);
    }
}
