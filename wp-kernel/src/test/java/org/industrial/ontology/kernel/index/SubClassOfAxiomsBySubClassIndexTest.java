package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.Class;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.SubClassOf;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.SubClassOfAxiomsBySubClassIndexImpl_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-09
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubClassOfAxiomsBySubClassIndexTest {

    private SubClassOfAxiomsBySubClassIndex impl;

    @Mock
    private OWLOntologyID ontologyID;

    private OWLClass cls;

    private OWLSubClassOfAxiom axiom;

    @Mock
    private OWLClassExpression superCls;

    @BeforeEach
    public void setUp() {
        cls = Class(mock(IRI.class));
        axiom = SubClassOf(cls, superCls);
        impl = new SubClassOfAxiomsBySubClassIndex();
        impl.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyID, axiom)));
    }

    @Test
    public void shouldGetSubClassOfAxiomForSubClass() {
        var axioms = impl.getSubClassOfAxiomsForSubClass(cls, ontologyID).collect(toSet());
        assertThat(axioms, hasItem(axiom));
    }

    @Test
    public void shouldGetEmptySetForUnknownOntologyId() {
        var axioms = impl.getSubClassOfAxiomsForSubClass(cls, mock(OWLOntologyID.class)).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @Test
    public void shouldGetEmptySetForUnknownClass() {
        var axioms = impl.getSubClassOfAxiomsForSubClass(mock(OWLClass.class), ontologyID).collect(toSet());
        assertThat(axioms.isEmpty(), is(true));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullOntologyId() {
        assertThrows(NullPointerException.class, () -> {
            impl.getSubClassOfAxiomsForSubClass(cls, null);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeForNullCls() {
        assertThrows(NullPointerException.class, () -> {
            impl.getSubClassOfAxiomsForSubClass(null, ontologyID);
        });
    }
}
