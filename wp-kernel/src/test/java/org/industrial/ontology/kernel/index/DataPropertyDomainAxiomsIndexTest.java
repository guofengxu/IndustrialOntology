package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DataPropertyDomainAxiomsIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataPropertyDomainAxiomsIndexTest {

    private DataPropertyDomainAxiomsIndex impl;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLDataProperty property;

    @Mock
    private OWLDataPropertyDomainAxiom axiom;

    @Mock
    private AxiomsByTypeIndex axiomsByTypeIndex;

    @BeforeEach
    public void setUp() {
        when(axiom.getProperty()).thenReturn(property);
        when(axiomsByTypeIndex.getAxiomsByType(any(), any())).thenAnswer(invocation -> Stream.of());
        when(axiomsByTypeIndex.getAxiomsByType(AxiomType.DATA_PROPERTY_DOMAIN, ontologyId)).thenAnswer(invocation -> Stream.of(axiom));
        impl = new DataPropertyDomainAxiomsIndex(axiomsByTypeIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), contains(axiomsByTypeIndex));
    }

    @Test
    public void shouldGetDataPropertyDomainAxiomForProperty() {
        var axioms = impl.getDataPropertyDomainAxioms(property, ontologyId).collect(toSet());
        assertThat(axioms, hasItem(axiom));
    }

    @Test
    public void shouldGetEmptySetForUnknownOntologyId() {
        var axioms = impl.getDataPropertyDomainAxioms(property, mock(OWLOntologyID.class)).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @Test
    public void shouldGetEmptySetForUnknownClass() {
        var axioms = impl.getDataPropertyDomainAxioms(mock(OWLDataProperty.class), ontologyId).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullOntologyId() {
        assertThrows(NullPointerException.class, () -> {
            impl.getDataPropertyDomainAxioms(property, null);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullProperty() {
        assertThrows(NullPointerException.class, () -> {
            impl.getDataPropertyDomainAxioms(null, ontologyId);
        });
    }
}
