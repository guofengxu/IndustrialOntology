package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.match.Matcher;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.AnnotationValuesAreNotDisjointMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AnnotationValuesAreNotDisjointMatcherTest {

    private AnnotationValuesAreNotDisjointMatcher matcher;

    @Mock
    private AnnotationAssertionAxiomsIndex hasAxioms;

    private Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<>();

    @Mock
    private Matcher<OWLAnnotationProperty> propAMatcher, propBMatcher;

    @Mock
    private OWLAnnotationValue annoValue, otherAnnoValue;

    @Mock
    private OWLAxiom axiom;

    @Mock
    private OWLAnnotationProperty propertyA, propertyB;

    @Mock
    private OWLAnnotationAssertionAxiom axA, axB;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI iri;

    @BeforeEach
    public void setUp() {
        matcher = new AnnotationValuesAreNotDisjointMatcher(hasAxioms, propAMatcher, propBMatcher);
        when(entity.getIRI()).thenReturn(iri);
        when(hasAxioms.getAnnotationAssertionAxioms(any())).thenReturn(axioms.stream());
        when(propAMatcher.matches(propertyA)).thenReturn(true);
        when(propBMatcher.matches(propertyB)).thenReturn(true);
        when(axA.getProperty()).thenReturn(propertyA);
        when(axB.getProperty()).thenReturn(propertyB);
        axioms.add(axA);
        axioms.add(axB);
    }

    @Test
    public void shouldNotMatchDifferentValues() {
        when(axA.getValue()).thenReturn(annoValue);
        when(axB.getValue()).thenReturn(otherAnnoValue);
        MatcherAssert.assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldMatchSameValue() {
        when(axA.getValue()).thenReturn(annoValue);
        when(axB.getValue()).thenReturn(annoValue);
        MatcherAssert.assertThat(matcher.matches(entity), is(true));
    }
}
