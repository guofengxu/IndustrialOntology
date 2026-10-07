package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.ClassFrame_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ClassFrameTest {

    private ClassFrame classFrame;

    private ClassFrame otherClassFrame;

    @Mock
    private OWLClassData subject;

    @Mock
    private OWLClassData cls;

    @Mock
    private PropertyValue propertyValue;

    private ImmutableSet<OWLClassData> classes;

    private ImmutableSet<PropertyValue> propertyValues;

    @BeforeEach
    public void setUp() throws Exception {
        classes = ImmutableSet.of(cls);
        propertyValues = ImmutableSet.of(propertyValue);
        classFrame = ClassFrame.get(subject, classes, propertyValues);
        otherClassFrame = ClassFrame.get(subject, classes, propertyValues);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIfSubjectIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ClassFrame.get(null, classes, propertyValues);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIfClassesIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ClassFrame.get(subject, null, propertyValues);
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIfPropertyValuesIsNull() {
        assertThrows(NullPointerException.class, () -> {
            ClassFrame.get(subject, classes, null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(classFrame, is(equalTo(classFrame)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(classFrame, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(classFrame, is(equalTo(otherClassFrame)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(classFrame.hashCode(), is(otherClassFrame.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(classFrame.toString(), startsWith("ClassFrame"));
    }

    @Test
    public void shouldReturnSuppliedSubject() {
        assertThat(classFrame.getSubject(), is(subject));
    }

    @Test
    public void shouldReturnSuppliedPropertyValues() {
        assertThat(classFrame.getPropertyValues(), hasItems(propertyValue));
    }
}
