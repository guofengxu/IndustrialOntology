package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.NamedIndividualFrame_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NamedIndividualFrameTest {

    private NamedIndividualFrame namedIndividualFrame;

    private NamedIndividualFrame otherNamedIndividualFrame;

    @Mock
    private OWLNamedIndividualData subject;

    private ImmutableSet<OWLClassData> types;

    @Mock
    private OWLClassData typeA, typeB;

    @Mock
    private PropertyValue propertyValue;

    private ImmutableSet<PropertyValue> propertyValueList;

    private ImmutableSet<OWLNamedIndividualData> sameIndividuals;

    @Mock
    private OWLNamedIndividualData individualA, individualB;

    @BeforeEach
    public void setUp() throws Exception {
        types = ImmutableSet.of(typeA, typeB);
        sameIndividuals = ImmutableSet.of(individualA, individualB);
        propertyValueList = ImmutableSet.of(propertyValue);
        namedIndividualFrame = NamedIndividualFrame.get(subject, types, propertyValueList, sameIndividuals);
        otherNamedIndividualFrame = NamedIndividualFrame.get(subject, types, propertyValueList, sameIndividuals);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_Subject_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            NamedIndividualFrame.get(null, types, propertyValueList, sameIndividuals);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_Types_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            NamedIndividualFrame.get(subject, null, propertyValueList, sameIndividuals);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_PropertyValues_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            NamedIndividualFrame.get(subject, types, null, sameIndividuals);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_SameIndividuals_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            NamedIndividualFrame.get(subject, types, propertyValueList, null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(namedIndividualFrame, is(equalTo(namedIndividualFrame)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(namedIndividualFrame, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(namedIndividualFrame, is(equalTo(otherNamedIndividualFrame)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(namedIndividualFrame.hashCode(), is(otherNamedIndividualFrame.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(namedIndividualFrame.toString(), startsWith("NamedIndividualFrame"));
    }

    @Test
    public void shouldReturnSupplied_Subject() {
        assertThat(namedIndividualFrame.getSubject(), is(subject));
    }

    @Test
    public void shouldReturnSupplied_Types() {
        assertThat(namedIndividualFrame.getClasses(), is(types));
    }

    @Test
    public void shouldReturnSupplied_PropertyValues() {
        assertThat(ImmutableSet.copyOf(namedIndividualFrame.getPropertyValues()), is(propertyValueList));
    }

    @Test
    public void shouldReturnSupplied_SameIndividuals() {
        assertThat(namedIndividualFrame.getSameIndividuals(), is(sameIndividuals));
    }
}
