package org.industrial.ontology.kernel.io.merge;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.project.Ontology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.semanticweb.owlapi.model.OWLAxiom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.AxiomDiffCalculator_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AxiomDiffCalculatorTest {

    private AxiomDiffCalculator calculator;

    @Mock
    private Ontology fromOnt;

    @Mock
    private Ontology toOnt;

    @Mock
    private OWLAxiom axiomA, axiomB, axiomC;

    @BeforeEach
    public void setUp() {
        calculator = new AxiomDiffCalculator();
        when(fromOnt.getAxioms())
                .thenReturn(ImmutableSet.of(axiomA, axiomB));
        when(toOnt.getAxioms())
                .thenReturn(ImmutableSet.of(axiomB, axiomC));
    }

    @Test
    public void shouldGetAddedAxioms() {
        var diff = calculator.computeDiff(fromOnt, toOnt);
        var addedAxioms = diff.getAdded();
        assertThat(addedAxioms, contains(axiomC));
    }

    @Test
    public void shouldGetRemovedAxioms() {
        var diff = calculator.computeDiff(fromOnt, toOnt);
        var removedAxioms = diff.getRemoved();
        assertThat(removedAxioms, contains(axiomA));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfFromOntologyIsNull() {
        assertThrows(NullPointerException.class, () -> {
            calculator.computeDiff(null, toOnt);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNpeIfToOntologyIsNull() {
        assertThrows(NullPointerException.class, () -> {
            calculator.computeDiff(fromOnt, null);
        });
    }
}
