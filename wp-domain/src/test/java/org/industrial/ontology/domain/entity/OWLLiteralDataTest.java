package org.industrial.ontology.domain.entity;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.semanticweb.owlapi.model.OWLLiteral;
import java.util.Optional;
import static org.industrial.ontology.domain.core.PrimitiveType.LITERAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.entity.OWLLiteralData_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OWLLiteralDataTest {

    private static final String THE_LANG = "TheLang";

    private static final String THE_LITERAL = "TheLiteral";

    private OWLLiteralData data;

    @Mock
    private OWLLiteral object;

    @BeforeEach
    public void setUp() {
        data = OWLLiteralData.get(object);
        when(object.getLang()).thenReturn(THE_LANG);
        when(object.hasLang()).thenReturn(true);
        when(object.getLiteral()).thenReturn(THE_LITERAL);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_object_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            OWLLiteralData.get(null);
        });
    }

    @Test
    public void shouldReturnSupplied_object() {
        assertThat(data.getObject(), is(this.object));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(data, is(data));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(data.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(data, is(OWLLiteralData.get(object)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_object() {
        assertThat(data, is(not(OWLLiteralData.get(Mockito.mock(OWLLiteral.class)))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(data.hashCode(), is(OWLLiteralData.get(object).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(data.toString(), Matchers.startsWith("OWLLiteralData"));
    }

    @Test
    public void should_getType() {
        assertThat(data.getType(), is(LITERAL));
    }

    @Test
    public void should_getLiteral() {
        assertThat(data.getLiteral(), is(object));
    }

    @Test
    public void should_getBrowserText() {
        assertThat(data.getBrowserText(), is(THE_LITERAL));
    }

    @Test
    public void should_getUnquotedBrowserText() {
        assertThat(data.getUnquotedBrowserText(), is(THE_LITERAL));
    }

    @Test
    public void should_getLexicalForm() {
        assertThat(data.getLexicalForm(), is(THE_LITERAL));
    }

    @Test
    public void shouldReturn_true_For_hasLang() {
        assertThat(data.hasLang(), is(true));
    }

    @Test
    public void should_getLang() {
        assertThat(data.getLang(), is(THE_LANG));
    }

    @Test
    public void should_asAnnotationValue() {
        assertThat(data.asAnnotationValue(), is(Optional.of(object)));
    }
}
