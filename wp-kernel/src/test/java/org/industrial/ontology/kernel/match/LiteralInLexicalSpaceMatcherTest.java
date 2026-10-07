package org.industrial.ontology.kernel.match;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.LiteralInLexicalSpaceMatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 8 Jun 2018
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LiteralInLexicalSpaceMatcherTest {

    private LiteralInLexicalSpaceMatcher matcher;

    @Mock
    private OWLLiteral literal;

    @Mock
    private OWLDatatype datatype;

    @BeforeEach
    public void setUp() {
        matcher = new LiteralInLexicalSpaceMatcher();
        when(literal.getDatatype()).thenReturn(datatype);
        when(datatype.isBuiltIn()).thenReturn(true);
        when(datatype.getBuiltInDatatype()).thenReturn(OWL2Datatype.XSD_INTEGER);
    }

    @Test
    public void shouldMatchLiteralInLexicalSpace() {
        when(literal.getLiteral()).thenReturn("33");
        assertThat(matcher.matches(literal), is(true));
    }

    @Test
    public void shouldNotMatchLiteralNotInLexicalSpace() {
        when(literal.getLiteral()).thenReturn("abc");
        assertThat(matcher.matches(literal), is(false));
    }
}
