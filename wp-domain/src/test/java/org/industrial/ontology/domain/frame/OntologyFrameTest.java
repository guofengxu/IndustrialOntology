package org.industrial.ontology.domain.frame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.HashSet;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.OntologyFrame_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyFrameTest {

    private OntologyFrame frame;

    private OWLOntologyID subject = new OWLOntologyID();

    @Mock
    private PropertyValueList propertyValues;

    private String shortForm = "The shortForm";

    @BeforeEach
    public void setUp() throws Exception {
        frame = new OntologyFrame(subject, propertyValues, shortForm);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_subject_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new OntologyFrame(null, propertyValues, shortForm);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_propertyValues_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new OntologyFrame(subject, null, shortForm);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_shortForm_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new OntologyFrame(subject, propertyValues, null);
        });
    }

    @Test
    public void shouldReturnSupplied_shortForm() {
        assertThat(frame.getShortForm(), is(this.shortForm));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(frame, is(frame));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(frame.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(frame, is(new OntologyFrame(subject, propertyValues, shortForm)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_subject() {
        assertThat(frame, is(not(new OntologyFrame(new OWLOntologyID(), propertyValues, shortForm))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_propertyValues() {
        assertThat(frame, is(not(new OntologyFrame(subject, mock(PropertyValueList.class), shortForm))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_shortForm() {
        assertThat(frame, is(not(new OntologyFrame(subject, propertyValues, "String-ed7172d0-40e4-4cec-ad6d-8ca1bceae7d1"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(frame.hashCode(), is(new OntologyFrame(subject, propertyValues, shortForm).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(frame.toString(), startsWith("OntologyFrame"));
    }

    @Test
    public void should_getPropertyValueList() {
        assertThat(frame.getPropertyValueList(), is(propertyValues));
    }

    @Test
    public void should_getSignature() {
        Set<OWLEntity> entitySet = new HashSet<>();
        entitySet.add(mock(OWLEntity.class));
        when(propertyValues.getSignature()).thenReturn(entitySet);
        assertThat(frame.getSignature(), is(propertyValues.getSignature()));
    }
}
