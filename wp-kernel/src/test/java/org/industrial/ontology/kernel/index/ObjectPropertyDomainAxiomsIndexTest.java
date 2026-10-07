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
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ObjectPropertyDomainAxiomsIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ObjectPropertyDomainAxiomsIndexTest {

    private ObjectPropertyDomainAxiomsIndex impl;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLObjectProperty property;

    @Mock
    private OWLObjectPropertyDomainAxiom axiom;

    @Mock
    private AxiomsByTypeIndex axiomsByTypeIndex;

    @BeforeEach
    public void setUp() {
        when(axiom.getProperty()).thenReturn(property);
        when(axiomsByTypeIndex.getAxiomsByType(any(), any())).thenAnswer(invocation -> Stream.of());
        when(axiomsByTypeIndex.getAxiomsByType(AxiomType.OBJECT_PROPERTY_DOMAIN, ontologyId)).thenAnswer(invocation -> Stream.of(axiom));
        impl = new ObjectPropertyDomainAxiomsIndex(axiomsByTypeIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), contains(axiomsByTypeIndex));
    }

    @Test
    public void shouldGetObjectPropertyDomainAxiomForProperty() {
        var axioms = impl.getObjectPropertyDomainAxioms(property, ontologyId).collect(toSet());
        assertThat(axioms, hasItem(axiom));
    }

    @Test
    public void shouldGetEmptySetForUnknownOntologyId() {
        var axioms = impl.getObjectPropertyDomainAxioms(property, mock(OWLOntologyID.class)).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @Test
    public void shouldGetEmptySetForUnknownClass() {
        var axioms = impl.getObjectPropertyDomainAxioms(mock(OWLObjectProperty.class), ontologyId).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullOntologyId() {
        assertThrows(NullPointerException.class, () -> {
            impl.getObjectPropertyDomainAxioms(property, null);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullProperty() {
        assertThrows(NullPointerException.class, () -> {
            impl.getObjectPropertyDomainAxioms(null, ontologyId);
        });
    }
}
