package org.industrial.ontology.domain.core;



import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.DataFactory_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 28 Aug 2018
 */
public class DataFactoryTest {


    @Test
    public void test_getFreshEntityReturnsFreshEntity() {
        OWLClass cls = DataFactory.getFreshOWLEntity(EntityType.CLASS, "X", Optional.empty());
        assertTrue(DataFactory.isFreshEntity(cls));
        assertEquals("X", DataFactory.getFreshEntityShortName(cls));
    }

    @Test
    public void test_getFreshEntityReturnsFreshEntityWithNoLangTag() {
        OWLClass cls = DataFactory.getFreshOWLEntity(EntityType.CLASS, "X", Optional.empty());
        assertTrue(DataFactory.isFreshEntity(cls));
        assertEquals("X", DataFactory.getFreshEntityShortName(cls));
        assertEquals(Optional.empty(), DataFactory.getFreshEntityLangTag(cls));
    }

    @Test
    public void test_getFreshEntityReturnsFreshEntityWithLangTag() {
        OWLClass cls = DataFactory.getFreshOWLEntity(EntityType.CLASS, "X", Optional.of("en"));
        assertTrue(DataFactory.isFreshEntity(cls));
        assertEquals("X", DataFactory.getFreshEntityShortName(cls));
        assertEquals(Optional.of("en"), DataFactory.getFreshEntityLangTag(cls));
    }

    @Test
    public void test_getFreshEntityReturnsFreshEntityWithLangTagTrimmed() {
        OWLClass cls = DataFactory.getFreshOWLEntity(EntityType.CLASS, "X", Optional.of("en "));
        assertTrue(DataFactory.isFreshEntity(cls));
        assertEquals("X", DataFactory.getFreshEntityShortName(cls));
        assertEquals(Optional.of("en"), DataFactory.getFreshEntityLangTag(cls));
    }
}
