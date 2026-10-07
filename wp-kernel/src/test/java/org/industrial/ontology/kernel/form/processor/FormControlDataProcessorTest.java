package org.industrial.ontology.kernel.form.processor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.form.FormFrameBuilder;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import java.util.Optional;
import org.industrial.ontology.domain.form.data.EntityNameControlData;
import org.industrial.ontology.domain.form.data.FormData;
import org.industrial.ontology.domain.form.data.GridControlData;
import org.industrial.ontology.domain.form.data.GridRowData;
import org.industrial.ontology.domain.form.data.ImageControlData;
import org.industrial.ontology.domain.form.data.IriFormControlData;
import org.industrial.ontology.domain.form.data.LiteralFormControlData;
import org.industrial.ontology.domain.form.data.MultiChoiceControlData;
import org.industrial.ontology.domain.form.data.NumberControlData;
import org.industrial.ontology.domain.form.data.SingleChoiceControlData;
import org.industrial.ontology.domain.form.data.TextControlData;
import org.industrial.ontology.domain.form.field.EntityNameControlDescriptor;
import org.industrial.ontology.domain.form.field.FormRegionOrdering;
import org.industrial.ontology.domain.form.field.GridControlDescriptor;
import org.industrial.ontology.domain.form.field.ImageControlDescriptor;
import org.industrial.ontology.domain.form.field.MultiChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.form.field.OwlBinding;
import org.industrial.ontology.domain.form.field.SingleChoiceControlDescriptor;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.industrial.ontology.domain.form.data.PrimitiveFormControlData;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.processor.FormControlDataProcessor_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-04-26
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FormControlDataProcessorTest {

    private FormControlDataProcessor processor;

    @Mock
    private GridControlDataProcessor gridControlDataProcessor;

    @Mock
    private FormDataProcessor formDataProcessor;

    @Mock
    private OWLLiteral literal;

    @Mock
    private FormFrameBuilder formFrameBuilder;

    @Mock
    private OwlBinding binding;

    @Mock
    private OWLEntity entity;

    @Mock
    private IRI iri;

    private ImmutableSet<FormRegionOrdering> ordering = ImmutableSet.of();

    @BeforeEach
    public void setUp() {
        processor = new FormControlDataProcessor(gridControlDataProcessor, () -> formDataProcessor);
    }

    @Test
    public void shouldProcessTextControlData() {
        var textControlData = TextControlData.get(mock(TextControlDescriptor.class), literal);
        processor.processFormControlData(binding, textControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addLiteralValue(binding, literal);
    }

    @Test
    public void shouldProcessNumberControlData() {
        var numberControlData = NumberControlData.get(mock(NumberControlDescriptor.class), literal);
        processor.processFormControlData(binding, numberControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addLiteralValue(binding, literal);
    }

    @Test
    public void shouldProcessEntityControlData() {
        var entityNameControlDescriptor = mock(EntityNameControlDescriptor.class);
        var entityControlData = EntityNameControlData.get(entityNameControlDescriptor, entity);
        processor.processFormControlData(binding, entityControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addEntityValue(binding, entity);
    }

    @Test
    public void shouldProcessImageControlData() {
        var imageControlDescriptor = mock(ImageControlDescriptor.class);
        var entityControlData = ImageControlData.get(imageControlDescriptor, iri);
        processor.processFormControlData(binding, entityControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addIriValue(binding, iri);
    }

    @Test
    public void shouldProcessMultiChoiceControlData() {
        var multiChoiceControlDescriptor = mock(MultiChoiceControlDescriptor.class);
        var iriFormControlValue = IriFormControlData.get(iri);
        var literalFormControlValue = LiteralFormControlData.get(literal);
        var values = ImmutableList.<PrimitiveFormControlData>of(iriFormControlValue, literalFormControlValue);
        var multiChoiceControlData = MultiChoiceControlData.get(multiChoiceControlDescriptor, values);
        processor.processFormControlData(binding, multiChoiceControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addIriValue(binding, iri);
        verify(formFrameBuilder, times(1)).addLiteralValue(binding, literal);
    }

    @Test
    public void shouldProcessSingleChoiceControlData() {
        var singleChoiceControlDescriptor = mock(SingleChoiceControlDescriptor.class);
        var iriFormControlValue = IriFormControlData.get(iri);
        var singleChoiceControlData = SingleChoiceControlData.get(singleChoiceControlDescriptor, iriFormControlValue);
        processor.processFormControlData(binding, singleChoiceControlData, formFrameBuilder);
        verify(formFrameBuilder, times(1)).addIriValue(binding, iri);
    }

    @Test
    public void shouldProcessGridControlData() {
        var gridControlDescriptor = mock(GridControlDescriptor.class);
        var page = new Page<>(1, 1, ImmutableList.of(mock(GridRowData.class)), 1);
        var gridControlData = GridControlData.get(gridControlDescriptor, page, ordering);
        processor.processFormControlData(binding, gridControlData, formFrameBuilder);
        verify(gridControlDataProcessor, times(1)).processGridControlData(binding, gridControlData, formFrameBuilder);
    }

    @Test
    public void shouldProcessFormData() {
        var formData = FormData.get(Optional.empty(), mock(FormDescriptor.class), ImmutableList.of());
        processor.processFormControlData(binding, formData, formFrameBuilder);
        verify(formDataProcessor, times(1)).processFormData(formData, true);
    }
}
