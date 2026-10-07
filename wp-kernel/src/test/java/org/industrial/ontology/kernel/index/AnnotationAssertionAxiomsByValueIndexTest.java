package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.AnnotationAssertionAxiomsByValueIndexImpl_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnnotationAssertionAxiomsByValueIndexTest {

    @Mock
    private OWLOntologyID ontologyId;

    @Mock
    private OWLAnnotationAssertionAxiom axiom;

    @Mock
    private IRI iriValue;

    @Mock
    private OWLAnonymousIndividual individualValue;

    @Mock
    private OWLLiteral literalValue;

    private AnnotationAssertionAxiomsByValueIndex index;

    @BeforeEach
    public void setUp() throws Exception {
        index = new AnnotationAssertionAxiomsByValueIndex();
    }

    @Test
    public void shouldIndexAxiomByIriValue() {
        when(axiom.getValue()).thenReturn(iriValue);
        index.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyId, axiom)));
        assertThat(index.getAxiomsByValue(iriValue, ontologyId).collect(toSet()), contains(axiom));
    }

    @Test
    public void shouldIndexAxiomByAnonymousIndividualValue() {
        when(axiom.getValue()).thenReturn(individualValue);
        index.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyId, axiom)));
        assertThat(index.getAxiomsByValue(individualValue, ontologyId).collect(toSet()), contains(axiom));
    }

    @Test
    public void shouldNotIndexAxiomByLiteralValue() {
        when(axiom.getValue()).thenReturn(literalValue);
        index.applyChanges(ImmutableList.of(AddAxiomChange.of(ontologyId, axiom)));
        assertThat(index.getAxiomsByValue(literalValue, ontologyId).collect(toSet()), not(contains(axiom)));
    }

    /**
     * @noinspection ConstantConditions
     */
    @Test
    public void shouldThrowNpeIfAxiomIsNull() {
        assertThrows(NullPointerException.class, () -> {
            index.getAxiomsByValue(null, ontologyId);
        });
    }

    /**
     * @noinspection ConstantConditions
     */
    @Test
    public void shouldThrowNpeIfOntologyIdIsNull() {
        assertThrows(NullPointerException.class, () -> {
            index.getAxiomsByValue(iriValue, null);
        });
    }
}
