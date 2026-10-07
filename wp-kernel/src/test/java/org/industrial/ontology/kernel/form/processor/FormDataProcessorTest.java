package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptorMissingException;
import org.industrial.ontology.domain.form.data.FormData;
import org.industrial.ontology.domain.form.data.FormFieldData;
import org.industrial.ontology.domain.form.data.FormSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormDataProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormDataProcessorTest {

    private FormDataProcessor processor;

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private FormFieldProcessor formFieldProcessor;

    @Mock
    private FormData formData;

    @Mock
    private FormFieldData formFieldData;

    @Mock
    private FormSubject subject;

    @Mock
    private FormDescriptor formDescriptor;

    @Mock
    private FormSubjectFactoryDescriptor subjectFactoryDescriptor;

    @BeforeEach
    public void setUp() {
        processor = new FormDataProcessor(() -> formFrameBuilder, formFieldProcessor);
        when(formData.getFormFieldData()).thenReturn(ImmutableList.of(formFieldData));
        when(formData.getSubject()).thenReturn(Optional.of(subject));
        when(formData.getFormDescriptor()).thenReturn(formDescriptor);
        when(formDescriptor.getSubjectFactoryDescriptor()).thenReturn(Optional.of(subjectFactoryDescriptor));
    }

    @Test
    public void shouldUseFormFrameBuilder() {
        var ffb = processor.processFormData(formData, false);
        assertThat(ffb, is(formFrameBuilder));
    }

    @Test
    public void shouldProcessSubject() {
        processor.processFormData(formData, false);
        verify(formFrameBuilder, times(1)).setSubject(subject);
    }

    @Test
    public void shouldProcessFormFieldData() {
        processor.processFormData(formData, false);
        verify(formFieldProcessor, times(1)).processFormFieldData(formFieldData, formFrameBuilder);
    }

    @Test
    public void shouldSetSubjectFactory() {
        processor.processFormData(formData, false);
        verify(formFrameBuilder, times(1)).setSubjectFactoryDescriptor(subjectFactoryDescriptor);
    }

    @Test
    public void shouldThrowExceptionIfSubjectFactoryDescriptorIsMissing() {
        assertThrows(FormSubjectFactoryDescriptorMissingException.class, () -> {
            when(formDescriptor.getSubjectFactoryDescriptor()).thenReturn(Optional.empty());
            processor.processFormData(formData, true);
        });
    }
}
