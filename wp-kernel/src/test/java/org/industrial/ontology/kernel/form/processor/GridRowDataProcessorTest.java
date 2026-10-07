package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormSubjectFactoryDescriptor;
import org.industrial.ontology.domain.form.GridColumnBindingMissingException;
import org.industrial.ontology.domain.form.data.FormSubject;
import org.industrial.ontology.domain.form.data.GridCellData;
import org.industrial.ontology.domain.form.data.GridRowData;
import org.industrial.ontology.domain.form.field.GridColumnDescriptor;
import org.industrial.ontology.domain.form.field.GridColumnId;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.GridRowDataProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GridRowDataProcessorTest {

    private GridRowDataProcessor processor;

    @Mock
    private FormFrameBuilder formFrameBuilder, rowformFrameBuilder;

    @Mock
    private GridCellDataProcessor gridCellDataProcessor;

    @Mock
    private OwlBinding binding;

    @Mock
    private FormSubjectFactoryDescriptor rowSubjectFactoryDescriptor;

    @Mock
    private GridColumnDescriptor columnDescriptor;

    @Mock
    private GridRowData gridRowData;

    @Mock
    private GridCellData gridCellData;

    @Mock
    private OwlBinding columnBinding;

    @BeforeEach
    public void setUp() {
        processor = new GridRowDataProcessor(() -> rowformFrameBuilder, gridCellDataProcessor);
        when(columnDescriptor.getId()).thenReturn(mock(GridColumnId.class));
        when(gridRowData.getCells()).thenReturn(ImmutableList.of(gridCellData));
        when(columnDescriptor.getOwlBinding()).thenReturn(Optional.of(columnBinding));
    }

    @Test
    public void shouldAddBindingForRow() {
        processor.processGridRowData(binding, rowSubjectFactoryDescriptor, ImmutableList.of(columnDescriptor), formFrameBuilder, gridRowData);
        verify(formFrameBuilder, times(1)).add(binding, rowformFrameBuilder);
    }

    @Test
    public void shouldNotSetSubjectIfRowDoesNotHaveSubject() {
        processor.processGridRowData(binding, rowSubjectFactoryDescriptor, ImmutableList.of(columnDescriptor), formFrameBuilder, gridRowData);
        verify(formFrameBuilder, never()).setSubject(any());
    }

    @Test
    public void shouldSetSubjectIfRowHasSubject() {
        var formSubject = mock(FormSubject.class);
        when(gridRowData.getSubject()).thenReturn(Optional.of(formSubject));
        processor.processGridRowData(binding, rowSubjectFactoryDescriptor, ImmutableList.of(columnDescriptor), formFrameBuilder, gridRowData);
        var rowFrameCapture = ArgumentCaptor.forClass(FormFrameBuilder.class);
        verify(formFrameBuilder, times(1)).add(eq(binding), rowFrameCapture.capture());
        verify(rowFrameCapture.getValue(), times(1)).setSubject(formSubject);
    }

    @Test
    public void shouldAddBindingForCells() {
        processor.processGridRowData(binding, rowSubjectFactoryDescriptor, ImmutableList.of(columnDescriptor), formFrameBuilder, gridRowData);
        verify(gridCellDataProcessor, times(1)).processGridCellData(rowformFrameBuilder, columnBinding, gridCellData);
    }

    @Test
    public void shouldThrowMissingBindingException() {
        assertThrows(GridColumnBindingMissingException.class, () -> {
            when(columnDescriptor.getOwlBinding()).thenReturn(Optional.empty());
            processor.processGridRowData(binding, rowSubjectFactoryDescriptor, ImmutableList.of(columnDescriptor), formFrameBuilder, gridRowData);
        });
    }
}
