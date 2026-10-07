package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLObjectPropertyData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ObjectPropertyFrame_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ObjectPropertyFrameTest {

    @Mock
    private OWLObjectPropertyData subject;

    private ImmutableSet<OWLClassData> domains;

    private ImmutableSet<OWLClassData> ranges;

    private ImmutableSet<PropertyAnnotationValue> annotations;

    private ImmutableSet<OWLObjectPropertyData> inverses;

    private ImmutableSet<ObjectPropertyCharacteristic> characteristics;

    private ObjectPropertyFrame objectPropertyFrame;

    private ObjectPropertyFrame otherObjectPropertyFrame;

    @Mock
    private OWLAnnotationProperty annotationProperty;

    @Mock
    private PropertyAnnotationValue annotationValue;

    @Mock
    private OWLClassData domain;

    @Mock
    private OWLClassData range;

    @Mock
    private OWLObjectPropertyData inverse;

    @BeforeEach
    public void setUp() throws Exception {
        annotations = ImmutableSet.of(annotationValue);
        domain = mock(OWLClassData.class);
        domains = ImmutableSet.of(domain);
        ranges = ImmutableSet.of(range);
        inverses = ImmutableSet.of(inverse);
        characteristics = ImmutableSet.of(ObjectPropertyCharacteristic.FUNCTIONAL, ObjectPropertyCharacteristic.INVERSE_FUNCTIONAL);
        objectPropertyFrame = ObjectPropertyFrame.get(subject, annotations, domains, ranges, inverses, characteristics);
        otherObjectPropertyFrame = ObjectPropertyFrame.get(subject, annotations, domains, ranges, inverses, characteristics);
    }

    @Test
    public void shouldThrowNullPointerExceptionIfSubjectIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(null, annotations, domains, ranges, inverses, characteristics);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIfAnnotationsIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(subject, null, domains, ranges, inverses, characteristics);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIfDomainsIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(subject, annotations, null, ranges, inverses, characteristics);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIfRangesIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(subject, annotations, domains, null, inverses, characteristics);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIfInversesIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(subject, annotations, domains, ranges, null, characteristics);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIfCharacteristicsIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ObjectPropertyFrame.get(subject, annotations, domains, ranges, inverses, null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(objectPropertyFrame, is(equalTo(objectPropertyFrame)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(objectPropertyFrame, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(objectPropertyFrame, is(equalTo(otherObjectPropertyFrame)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(objectPropertyFrame.hashCode(), is(otherObjectPropertyFrame.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(objectPropertyFrame.toString(), startsWith("ObjectPropertyFrame"));
    }

    @Test
    public void shouldReturnSuppliedSubject() {
        assertThat(objectPropertyFrame.getSubject(), is(subject));
    }

    @Test
    public void shouldReturnSuppliedAnnotations() {
        assertThat(objectPropertyFrame.getAnnotationPropertyValues(), is(annotations));
    }

    @Test
    public void shouldReturnSuppliedDomains() {
        assertThat(objectPropertyFrame.getDomains(), is(domains));
    }

    @Test
    public void shouldReturnSuppliedRanges() {
        assertThat(objectPropertyFrame.getRanges(), is(ranges));
    }

    @Test
    public void shouldReturnSuppliedCharacteristics() {
        assertThat(objectPropertyFrame.getCharacteristics(), is(characteristics));
    }
}
