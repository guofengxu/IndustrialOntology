package org.industrial.ontology.kernel.form;

import org.industrial.ontology.kernel.form.processor.FormDataConverter;
import org.industrial.ontology.kernel.form.processor.FormDataProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import org.industrial.ontology.domain.form.data.FormData;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormDataConverter_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormDataConverterTest {

    private FormDataConverter converter;

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private FormSubjectResolver formSubjectResolver;

    @Mock
    private FormDataProcessor formDataProcessor;

    @Mock
    private FormData formData;

    @Mock
    private FormFrame formFrame;

    @BeforeEach
    public void setUp() {
        converter = new FormDataConverter(formSubjectResolver, formDataProcessor);
        when(formDataProcessor.processFormData(formData, false)).thenReturn(formFrameBuilder);
        when(formFrameBuilder.build(formSubjectResolver)).thenReturn(formFrame);
    }

    @Test
    public void shouldCreateFormFrame() {
        // Check that the converter uses the supplied form data processor and resolver correctly
        var converted = converter.convert(formData);
        assertThat(converted, is(formFrame));
    }
}
