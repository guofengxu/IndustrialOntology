package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.ConflictingBooleanValuesMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ConflictingBooleanValuesMatcherTest {

    private ConflictingBooleanValuesMatcher matcher;

    @Mock
    private AnnotationAssertionAxiomsIndex hasAxioms;

    private Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<>();

    @Mock
    private OWLAnnotationAssertionAxiom axiomA, axiomB;

    @Mock
    private OWLAnnotationProperty prop, propA, propB;

    @Mock
    private OWLLiteral literalTrue, literalFalse;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI iri;

    @BeforeEach
    public void setUp() {
        axioms.clear();
        matcher = new ConflictingBooleanValuesMatcher(hasAxioms);
        when(axiomA.getProperty()).thenReturn(propA);
        when(axiomB.getProperty()).thenReturn(propB);
        when(entity.getIRI()).thenReturn(iri);
        when(hasAxioms.getAnnotationAssertionAxioms(any())).thenReturn(axioms.stream());
        axioms.add(axiomA);
        axioms.add(axiomB);
        when(literalTrue.isBoolean()).thenReturn(true);
        when(literalTrue.getLiteral()).thenReturn("true");
        when(literalFalse.isBoolean()).thenReturn(true);
        when(literalFalse.getLiteral()).thenReturn("false");
    }

    @Test
    public void shouldNotMatchDifferentPropertyDifferentValues() {
        when(axiomA.getValue()).thenReturn(literalTrue);
        when(axiomB.getValue()).thenReturn(literalFalse);
        assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldNotMatchDifferentPropertySameValues() {
        when(axiomA.getValue()).thenReturn(literalTrue);
        when(axiomB.getValue()).thenReturn(literalTrue);
        assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldMatchSamePropertyDifferentValues() {
        when(axiomA.getProperty()).thenReturn(propA);
        when(axiomB.getProperty()).thenReturn(propA);
        when(axiomA.getValue()).thenReturn(literalTrue);
        when(axiomB.getValue()).thenReturn(literalFalse);
        assertThat(matcher.matches(entity), is(true));
    }
}
