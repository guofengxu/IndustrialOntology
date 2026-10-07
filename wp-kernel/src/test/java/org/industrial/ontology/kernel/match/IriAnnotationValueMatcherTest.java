package org.industrial.ontology.kernel.match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.api.match.Matcher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.IriAnnotationValueMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Jun 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class IriAnnotationValueMatcherTest {

    private IriAnnotationValueMatcher matcher;

    @Mock
    private Matcher<IRI> iriMatcher;

    @Mock
    private IRI iri;

    @Mock
    private OWLLiteral literal;

    @Mock
    private OWLAnonymousIndividual individual;

    @BeforeEach
    public void setUp() {
        matcher = new IriAnnotationValueMatcher(iriMatcher);
    }

    @Test
    public void shouldNotMatchLiteral() {
        assertThat(matcher.matches(literal), is(false));
    }

    @Test
    public void shouldNotMatchAnonymousIndividual() {
        assertThat(matcher.matches(individual), is(false));
    }

    @Test
    public void shouldNotMatchIri() {
        assertThat(matcher.matches(iri), is(false));
    }

    @Test
    public void shouldMatchIri() {
        when(iriMatcher.matches(iri)).thenReturn(true);
        assertThat(matcher.matches(iri), is(true));
    }
}
