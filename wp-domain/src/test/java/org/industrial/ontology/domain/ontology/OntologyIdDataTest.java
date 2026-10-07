package org.industrial.ontology.domain.ontology;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyID;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.ontology.OntologyIdData_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyIdDataTest {

    private OntologyIdData ontologyIdData;

    private OWLOntologyID ontologyID = new OWLOntologyID();

    private String browserText = "The browserText";

    @BeforeEach
    public void setUp() throws Exception {
        ontologyIdData = OntologyIdData.get(ontologyID, browserText);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_ontologyID_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            OntologyIdData.get(null, browserText);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_browserText_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            OntologyIdData.get(ontologyID, null);
        });
    }

    @Test
    public void shouldReturnSupplied_browserText() {
        MatcherAssert.assertThat(ontologyIdData.getBrowserText(), is(this.browserText));
    }

    @Test
    public void shouldBeEqualToSelf() {
        MatcherAssert.assertThat(ontologyIdData, is(ontologyIdData));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        MatcherAssert.assertThat(ontologyIdData.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        MatcherAssert.assertThat(ontologyIdData, is(OntologyIdData.get(ontologyID, browserText)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_ontologyID() {
        MatcherAssert.assertThat(ontologyIdData, is(not(OntologyIdData.get(new OWLOntologyID(), browserText))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_browserText() {
        MatcherAssert.assertThat(ontologyIdData, is(not(OntologyIdData.get(ontologyID, "String-113be408-6933-4a00-833b-5df2376852f2"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        MatcherAssert.assertThat(ontologyIdData.hashCode(), is(OntologyIdData.get(ontologyID, browserText).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        MatcherAssert.assertThat(ontologyIdData.toString(), startsWith("OntologyIdData"));
    }

    @Test
    public void should_getUnquotedBrowserText() {
        MatcherAssert.assertThat(ontologyIdData.getUnquotedBrowserText(), is(browserText));
    }
}
