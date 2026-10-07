package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.data.FormControlData;
import org.industrial.ontology.domain.form.data.GridCellData;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridCellDataProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GridCellDataProcessorTest {

    private GridCellDataProcessor processor;

    @Mock
    private FormControlDataProcessor formControlDataProcessor;

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private OwlBinding binding;

    @Mock
    private GridCellData gridCellData;

    @Mock
    private FormControlData formControlData;

    private ImmutableList<FormControlData> values;

    @BeforeEach
    public void setUp() {
        values = ImmutableList.of(formControlData);
        processor = new GridCellDataProcessor(() -> formControlDataProcessor);
        when(gridCellData.getValues()).thenReturn(new Page<>(1, 1, values, values.size()));
    }

    @Test
    public void shouldProcessCellData() {
        processor.processGridCellData(formFrameBuilder, binding, gridCellData);
        verify(formControlDataProcessor, times(1)).processFormControlData(binding, formControlData, formFrameBuilder);
    }
}
