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
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubObjectPropertyAxiomsBySubPropertyIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubObjectPropertyAxiomsBySubPropertyIndexTest {

    private SubObjectPropertyAxiomsBySubPropertyIndex impl;

    @Mock
    private OWLOntologyID ontologyID;

    @Mock
    private OWLObjectProperty property;

    @Mock
    private OWLSubObjectPropertyOfAxiom axiom;

    @Mock
    private AxiomsByTypeIndex axiomsByTypeIndex;

    @BeforeEach
    public void setUp() {
        when(axiom.getSubProperty()).thenReturn(property);
        when(axiomsByTypeIndex.getAxiomsByType(any(), any())).thenAnswer(invocation -> Stream.empty());
        when(axiomsByTypeIndex.getAxiomsByType(AxiomType.SUB_OBJECT_PROPERTY, ontologyID)).thenAnswer(invocation -> Stream.of(axiom));
        impl = new SubObjectPropertyAxiomsBySubPropertyIndex(axiomsByTypeIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), contains(axiomsByTypeIndex));
    }

    @Test
    public void shouldGetSubObjectPropertyOfAxiomForProperty() {
        var axioms = impl.getSubPropertyOfAxioms(property, ontologyID).collect(toSet());
        assertThat(axioms, hasItem(axiom));
    }

    @Test
    public void shouldGetEmptySetForUnknownOntologyId() {
        var axioms = impl.getSubPropertyOfAxioms(property, mock(OWLOntologyID.class)).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @Test
    public void shouldGetEmptySetForUnknownProperty() {
        var axioms = impl.getSubPropertyOfAxioms(mock(OWLObjectProperty.class), ontologyID).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullOntologyId() {
        assertThrows(NullPointerException.class, () -> {
            impl.getSubPropertyOfAxioms(property, null);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullProperty() {
        assertThrows(NullPointerException.class, () -> {
            impl.getSubPropertyOfAxioms(null, ontologyID);
        });
    }
}
