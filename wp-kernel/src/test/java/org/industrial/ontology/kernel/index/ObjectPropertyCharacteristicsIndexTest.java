package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.index.AxiomsByTypeIndex;
import org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import static org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic.ASYMMETRIC;
import static org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic.FUNCTIONAL;
import static org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic.IRREFLEXIVE;
import static org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic.REFLEXIVE;
import static org.industrial.ontology.domain.frame.ObjectPropertyCharacteristic.TRANSITIVE;
import static org.semanticweb.owlapi.model.AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.TRANSITIVE_OBJECT_PROPERTY;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ObjectPropertyCharacteristicsIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-10
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ObjectPropertyCharacteristicsIndexTest {

    private ObjectPropertyCharacteristicsIndex impl;

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLObjectProperty property;

    @Mock
    private AxiomsByTypeIndex axiomsByTypeIndex;

    @BeforeEach
    public void setUp() {
        when(axiomsByTypeIndex.getAxiomsByType(any(), any())).thenAnswer(invocation -> Stream.empty());
        impl = new ObjectPropertyCharacteristicsIndex(axiomsByTypeIndex);
    }

    @Test
    public void shouldGetDependencies() {
        assertThat(impl.getDependencies(), contains(axiomsByTypeIndex));
    }

    private <A extends OWLObjectPropertyCharacteristicAxiom> Stream<A> mockAxiom(Class<A> cls) {
        A ax = mock(cls);
        when(ax.getProperty()).thenReturn(property);
        return Stream.of(ax);
    }

    @Test
    public void shouldReturnTrueForFunctional() {
        when(axiomsByTypeIndex.getAxiomsByType(FUNCTIONAL_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLFunctionalObjectPropertyAxiom.class));
        assertHasCharacteristic(ObjectPropertyCharacteristic.FUNCTIONAL, true);
    }

    private void assertHasCharacteristic(ObjectPropertyCharacteristic characteristic, boolean b) {
        var hasCharacteristic = impl.hasCharacteristic(property, characteristic, ontologyId);
        assertThat(hasCharacteristic, is(b));
    }

    @Test
    public void shouldReturnFalseForFunctional() {
        assertHasCharacteristic(ObjectPropertyCharacteristic.FUNCTIONAL, false);
    }

    @Test
    public void shouldReturnTrueForInverseFunctional() {
        when(axiomsByTypeIndex.getAxiomsByType(INVERSE_FUNCTIONAL_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLInverseFunctionalObjectPropertyAxiom.class));
        assertHasCharacteristic(ObjectPropertyCharacteristic.INVERSE_FUNCTIONAL, true);
    }

    @Test
    public void shouldReturnFalseForInverseFunctional() {
        assertHasCharacteristic(ObjectPropertyCharacteristic.INVERSE_FUNCTIONAL, false);
    }

    @Test
    public void shouldReturnTrueForSymmetric() {
        when(axiomsByTypeIndex.getAxiomsByType(SYMMETRIC_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLSymmetricObjectPropertyAxiom.class));
        assertHasCharacteristic(ObjectPropertyCharacteristic.SYMMETRIC, true);
    }

    @Test
    public void shouldReturnFalseForSymmetric() {
        assertHasCharacteristic(ObjectPropertyCharacteristic.SYMMETRIC, false);
    }

    @Test
    public void shouldReturnTrueForAsymmetric() {
        when(axiomsByTypeIndex.getAxiomsByType(ASYMMETRIC_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLAsymmetricObjectPropertyAxiom.class));
        assertHasCharacteristic(ASYMMETRIC, true);
    }

    @Test
    public void shouldReturnFalseForAsymmetric() {
        assertHasCharacteristic(ASYMMETRIC, false);
    }

    @Test
    public void shouldReturnTrueForReflexive() {
        when(axiomsByTypeIndex.getAxiomsByType(REFLEXIVE_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLReflexiveObjectPropertyAxiom.class));
        assertHasCharacteristic(REFLEXIVE, true);
    }

    @Test
    public void shouldReturnFalseForReflexive() {
        assertHasCharacteristic(REFLEXIVE, false);
    }

    @Test
    public void shouldReturnTrueForIrreflexive() {
        when(axiomsByTypeIndex.getAxiomsByType(IRREFLEXIVE_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLIrreflexiveObjectPropertyAxiom.class));
        assertHasCharacteristic(IRREFLEXIVE, true);
    }

    @Test
    public void shouldReturnFalseForIrreflexive() {
        assertHasCharacteristic(IRREFLEXIVE, false);
    }

    @Test
    public void shouldReturnTrueForTransitive() {
        when(axiomsByTypeIndex.getAxiomsByType(TRANSITIVE_OBJECT_PROPERTY, ontologyId)).thenAnswer(invocation -> mockAxiom(OWLTransitiveObjectPropertyAxiom.class));
        assertHasCharacteristic(TRANSITIVE, true);
    }

    @Test
    public void shouldReturnFalseForTransitive() {
        assertHasCharacteristic(TRANSITIVE, false);
    }

    @Test
    public void shouldReturnFalseForUnknownOntologyId() {
        assertThat(impl.hasCharacteristic(property, FUNCTIONAL, mock(OWLOntologyID.class)), is(false));
    }

    @Test
    public void shouldReturnFalseForUnknownProperty() {
        assertThat(impl.hasCharacteristic(mock(OWLObjectProperty.class), FUNCTIONAL, ontologyId), is(false));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfPropertyIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.hasCharacteristic(null, FUNCTIONAL, ontologyId);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfCharacteristicIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.hasCharacteristic(property, null, ontologyId);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfOntologyIdIsNull() {
        assertThrows(NullPointerException.class, () -> {
            impl.hasCharacteristic(property, FUNCTIONAL, null);
        });
    }
}
