package org.industrial.ontology.domain.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLDataPropertyData;
import org.industrial.ontology.domain.entity.OWLDatatypeData;
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
 * Ported from {@code edu.stanford.bmir.protege.web.shared.frame.DataPropertyFrame_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 15/01/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DataPropertyFrameTest {

    private DataPropertyFrame dataPropertyFrame;

    private DataPropertyFrame otherDataPropertyFrame;

    @Mock
    private OWLDataPropertyData subject;

    @Mock
    private ImmutableSet<PropertyValue> propertyValueList;

    private ImmutableSet<OWLClassData> domains;

    @Mock
    private OWLClassData domainA, domainB;

    @Mock
    private OWLDatatypeData rangeA, rangeB;

    private ImmutableSet<OWLDatatypeData> ranges;

    private boolean functional = true;

    @BeforeEach
    public void setUp() throws Exception {
        domains = ImmutableSet.of(domainA, domainB);
        ranges = ImmutableSet.of(rangeA, rangeB);
        dataPropertyFrame = DataPropertyFrame.get(subject, propertyValueList, domains, ranges, functional);
        otherDataPropertyFrame = DataPropertyFrame.get(subject, propertyValueList, domains, ranges, functional);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_Subject_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            DataPropertyFrame.get(null, propertyValueList, domains, ranges, functional);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_PropertyValueList_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            DataPropertyFrame.get(subject, null, domains, ranges, functional);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_Domains_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            DataPropertyFrame.get(subject, propertyValueList, null, ranges, functional);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_Ranges_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            DataPropertyFrame.get(subject, propertyValueList, domains, null, functional);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(dataPropertyFrame, is(equalTo(dataPropertyFrame)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(dataPropertyFrame, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(dataPropertyFrame, is(equalTo(otherDataPropertyFrame)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(dataPropertyFrame.hashCode(), is(otherDataPropertyFrame.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(dataPropertyFrame.toString(), startsWith("DataPropertyFrame"));
    }

    @Test
    public void shouldReturnSupplied_Subject() {
        assertThat(dataPropertyFrame.getSubject(), is(subject));
    }

    @Test
    public void shouldReturnSupplied_PropertyValueList() {
        assertThat(dataPropertyFrame.getPropertyValueList().getPropertyValues(), is(propertyValueList));
    }

    @Test
    public void shouldReturnSupplied_Domains() {
        assertThat(dataPropertyFrame.getDomains(), is(domains));
    }

    @Test
    public void shouldReturnSupplied_Ranges() {
        assertThat(dataPropertyFrame.getRanges(), is(ranges));
    }

    @Test
    public void shouldReturnSupplied_Functional() {
        assertThat(dataPropertyFrame.isFunctional(), is(true));
    }
}
