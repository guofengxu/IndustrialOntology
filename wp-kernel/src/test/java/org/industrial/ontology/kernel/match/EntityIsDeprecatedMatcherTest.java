package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsIndex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityIsDeprecatedMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityIsDeprecatedMatcherTest {

    private EntityIsDeprecatedMatcher matcher;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI iri;

    @Mock
    private AnnotationAssertionAxiomsIndex hasAxioms;

    @Mock
    private OWLAnnotationAssertionAxiom ax;

    @Mock
    private OWLAnnotation annotation;

    @Mock
    private OWLAnnotationProperty property;

    @Mock
    private OWLLiteral value;

    @Mock
    private OWLDatatype datatype;

    private Set<OWLAnnotationAssertionAxiom> axioms = new HashSet<>();

    @BeforeEach
    public void setUp() {
        matcher = new EntityIsDeprecatedMatcher(hasAxioms);
        when(value.getLiteral()).thenReturn("false");
        when(value.getDatatype()).thenReturn(datatype);
        when(value.getLang()).thenReturn("");
        when(entity.getIRI()).thenReturn(iri);
        when(hasAxioms.getAnnotationAssertionAxioms(iri)).thenReturn(axioms.stream());
        when(ax.getProperty()).thenReturn(property);
        when(ax.getValue()).thenReturn(value);
        axioms.add(ax);
    }

    @Test
    public void shouldNotMatchIsDeprecated() {
        when(property.isDeprecated()).thenReturn(false);
        assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldNotMatchIsDeprecatedForNonBooleanDatatype() {
        when(property.isDeprecated()).thenReturn(true);
        when(datatype.isBoolean()).thenReturn(false);
        when(value.getLiteral()).thenReturn("true");
        assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldNotMatchIsDeprecatedForBooleanFalse() {
        when(property.isDeprecated()).thenReturn(true);
        when(value.getLiteral()).thenReturn("false");
        assertThat(matcher.matches(entity), is(false));
    }

    @Test
    public void shouldMatchIsDeprecatedForBooleanTrue() {
        when(property.isDeprecated()).thenReturn(true);
        when(datatype.isBoolean()).thenReturn(true);
        when(value.getLiteral()).thenReturn("true");
        assertThat(matcher.matches(entity), is(true));
    }
}
