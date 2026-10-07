package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormFieldBindingMissingException;
import org.industrial.ontology.domain.form.data.FormControlData;
import org.industrial.ontology.domain.form.data.FormFieldData;
import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.form.field.FormFieldId;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Optional;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormFieldProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormFieldProcessorTest {

    private FormFieldProcessor formFieldProcessor;

    @Mock
    private FormControlDataProcessor formControlDataProcessor;

    @Mock
    private OwlBinding binding;

    @Mock
    private FormControlData formControlData;

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private FormFieldData fieldData;

    @Mock
    private FormFieldDescriptor formFieldDescriptor;

    @Mock
    private FormFieldId fieldId;

    @BeforeEach
    public void setUp() {
        formFieldProcessor = new FormFieldProcessor(formControlDataProcessor);
        when(fieldData.getFormFieldDescriptor()).thenReturn(formFieldDescriptor);
        when(formFieldDescriptor.getId()).thenReturn(fieldId);
        when(formFieldDescriptor.getOwlBinding()).thenReturn(Optional.of(binding));
        when(fieldData.getFormControlData()).thenReturn(new Page<>(1, 1, ImmutableList.of(formControlData), 1));
    }

    @Test
    public void shouldProcessFormControlData() {
        formFieldProcessor.processFormFieldData(fieldData, formFrameBuilder);
        verify(formControlDataProcessor, times(1)).processFormControlData(binding, formControlData, formFrameBuilder);
    }

    @Test
    public void shouldThrowMissingBindingExceptionIfBindingIsNotPresent() {
        assertThrows(FormFieldBindingMissingException.class, () -> {
            when(formFieldDescriptor.getOwlBinding()).thenReturn(Optional.empty());
            formFieldProcessor.processFormFieldData(fieldData, formFrameBuilder);
        });
    }
}
