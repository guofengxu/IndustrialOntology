package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.entity.OWLClassData;
import org.industrial.ontology.domain.entity.OWLNamedIndividualData;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.FormDataDto;
import org.industrial.ontology.domain.form.data.TextControlDataDto;
import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.form.field.FormFieldId;
import org.industrial.ontology.domain.form.field.OwlPropertyBinding;
import org.industrial.ontology.domain.form.field.SubFormControlDescriptor;
import org.industrial.ontology.domain.form.field.TextControlDescriptor;
import org.industrial.ontology.domain.frame.FrameComponentRenderer;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityFrameFormDataDtoBuilderFactoryTest {

    @Mock
    private FrameComponentRenderer frameComponentRenderer;

    @Mock
    private BindingValuesExtractor bindingValuesExtractor;

    @Mock
    private EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureByIriIndex;

    @Mock
    private MatchingEngine matchingEngine;

    @Mock
    private FormFilterMatcherFactory formFilterMatcherFactory;

    private EntityFrameFormDataDtoBuilderFactory factory;

    private OWLClass subject;

    private OWLLiteral subjectLabel;

    private OWLLiteral partLabel;

    private FormDescriptor formDescriptor;

    @BeforeEach
    public void setUp() {
        var dataFactory = new OWLDataFactoryImpl();
        subject = dataFactory.getOWLClass(IRI.create("http://example.org/Alanine"));
        OWLNamedIndividual part = dataFactory.getOWLNamedIndividual(IRI.create("http://example.org/part-1"));
        subjectLabel = dataFactory.getOWLLiteral("Alanine", "en");
        partLabel = dataFactory.getOWLLiteral("Side chain", "en");
        var labelBinding = OwlPropertyBinding.get(dataFactory.getRDFSLabel());
        var partBinding = OwlPropertyBinding.get(dataFactory.getOWLObjectProperty(
                IRI.create("http://example.org/hasPart")));

        when(frameComponentRenderer.getEntityRendering(subject)).thenReturn(OWLClassData.get(subject, ImmutableMap.of()));
        when(frameComponentRenderer.getEntityRendering(part)).thenReturn(OWLNamedIndividualData.get(part, ImmutableMap.of()));
        when(bindingValuesExtractor.getBindingValues(subject, labelBinding)).thenReturn(ImmutableList.of(subjectLabel));
        when(bindingValuesExtractor.getBindingValues(part, labelBinding)).thenReturn(ImmutableList.of(partLabel));
        when(bindingValuesExtractor.getBindingValues(subject, partBinding)).thenReturn(ImmutableList.of(part));

        var subForm = form(textField(labelBinding));
        formDescriptor = form(textField(labelBinding),
                              FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), partBinding,
                                                      LanguageMap.empty(), null, new SubFormControlDescriptor(subForm),
                                                      null, null, false, null, null));

        factory = new EntityFrameFormDataDtoBuilderFactory(frameComponentRenderer,
                                                           bindingValuesExtractor,
                                                           entitiesInProjectSignatureByIriIndex,
                                                           matchingEngine,
                                                           formFilterMatcherFactory);
    }

    @Test
    public void shouldBuildFormDataForTextAndSubFormFields() {
        var formData = createBuilder().toFormData(subject, formDescriptor);

        assertThat(formData.getFormFieldData().size(), is(2));
        assertThat(textValues(controlData(formData, 0)), contains(subjectLabel));
        // The sub-form is built through the supplier that closes the builder's dependency cycle.
        var subForms = controlData(formData, 1);
        assertThat(subForms.size(), is(1));
        assertThat(subForms.get(0), is(instanceOf(FormDataDto.class)));
        assertThat(textValues(controlData((FormDataDto) subForms.get(0), 0)), contains(partLabel));
    }

    @Test
    public void shouldShareRenderingCacheWithinOneRequestOnly() {
        var builder = createBuilder();
        builder.toFormData(subject, formDescriptor);
        builder.toFormData(subject, formDescriptor);
        verify(frameComponentRenderer, times(1)).getEntityRendering(subject);

        createBuilder().toFormData(subject, formDescriptor);
        verify(frameComponentRenderer, times(2)).getEntityRendering(subject);
    }

    @Test
    public void shouldCreateANewBuilderForEachRequest() {
        assertThat(createBuilder(), is(not(sameInstance(createBuilder()))));
    }

    private EntityFrameFormDataDtoBuilder createBuilder() {
        return factory.create(FormRegionOrderingIndex.get(ImmutableSet.of()),
                              LangTagFilter.get(ImmutableSet.of()),
                              FormPageRequestIndex.create(ImmutableList.of()),
                              FormRegionFilterIndex.get(ImmutableSet.of()));
    }

    private static FormDescriptor form(FormFieldDescriptor... fields) {
        return new FormDescriptor(FormId.get(UUID.randomUUID().toString()), LanguageMap.empty(), List.of(fields),
                                  Optional.empty());
    }

    private static FormFieldDescriptor textField(OwlPropertyBinding binding) {
        return FormFieldDescriptor.get(FormFieldId.get(UUID.randomUUID().toString()), binding, LanguageMap.empty(),
                                       null, TextControlDescriptor.getDefault(), null, null, false, null, null);
    }

    private static List<FormControlDataDto> controlData(FormDataDto formData, int field) {
        return formData.getFormFieldData().get(field).getFormControlData().getPageElements();
    }

    private static List<OWLLiteral> textValues(List<FormControlDataDto> controlData) {
        return controlData.stream()
                          .map(data -> ((TextControlDataDto) data).getValue())
                          .flatMap(Optional::stream)
                          .toList();
    }
}
