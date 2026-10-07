package org.industrial.ontology.kernel.axiom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.axiom.AxiomByRenderingComparator_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 04/02/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AxiomByRenderingComparatorTest {

    private AxiomByRenderingComparator renderingComparator;

    @Mock
    private OWLObjectRenderer renderer;

    @Mock
    private OWLAxiom axiom1, axiom2;

    @BeforeEach
    public void setUp() throws Exception {
        renderingComparator = new AxiomByRenderingComparator(renderer);
    }

    @Test
    public void shouldCompareByRendering() {
        when(renderer.render(axiom1)).thenReturn("A");
        when(renderer.render(axiom2)).thenReturn("B");
        assertThat(renderingComparator.compare(axiom1, axiom2), is(lessThan(0)));
    }

    @Test
    public void shouldIgnoreCase() {
        when(renderer.render(axiom1)).thenReturn("a");
        when(renderer.render(axiom2)).thenReturn("A");
        assertThat(renderingComparator.compare(axiom1, axiom2), is(0));
    }
}
