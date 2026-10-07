package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptorMissingException;
import org.industrial.ontology.domain.form.data.GridControlData;
import org.industrial.ontology.domain.form.data.GridRowData;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridControlDataProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GridControlDataProcessorTest {

    GridControlDataProcessor processor;

    @Mock
    private GridRowDataProcessor gridRowDataProcessor;

    @Mock
    private OwlBinding binding;

    @Mock
    private GridControlDescriptor gridControlDescriptor;

    private ImmutableList<GridColumnDescriptor> columns = ImmutableList.of(mock(GridColumnDescriptor.class));

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private FormSubjectFactoryDescriptor rowSubjectFactoryDescriptor;

    @Mock
    private GridRowData gridRowData;

    private Page<GridRowData> page;

    private GridControlData gridControlData;

    private ImmutableSet<FormRegionOrdering> ordering = ImmutableSet.of();

    @BeforeEach
    public void setUp() {
        processor = new GridControlDataProcessor(gridRowDataProcessor);
        when(gridControlDescriptor.getColumns()).thenReturn(columns);
        page = new Page<>(1, 1, ImmutableList.of(gridRowData), 1);
        gridControlData = GridControlData.get(gridControlDescriptor, page, ordering);
    }

    @Test
    public void shouldProcessGridRows() {
        when(gridControlDescriptor.getSubjectFactoryDescriptor()).thenReturn(Optional.of(rowSubjectFactoryDescriptor));
        processor.processGridControlData(binding, gridControlData, formFrameBuilder);
        verify(gridRowDataProcessor, times(1)).processGridRowData(binding, rowSubjectFactoryDescriptor, columns, formFrameBuilder, gridRowData);
    }

    @Test
    public void shouldThrowExceptionForMissingRowSubjectFactoryDescriptor() {
        assertThrows(FormSubjectFactoryDescriptorMissingException.class, () -> {
            when(gridControlDescriptor.getSubjectFactoryDescriptor()).thenReturn(Optional.empty());
            processor.processGridControlData(binding, gridControlData, formFrameBuilder);
        });
    }
}
