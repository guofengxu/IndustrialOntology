package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.data.EntityNameControlDataDto;
import org.industrial.ontology.domain.form.data.FormControlData;
import org.industrial.ontology.domain.form.data.FormControlDataDto;
import org.industrial.ontology.domain.form.data.FormData;
import org.industrial.ontology.domain.form.data.FormDataDto;
import org.industrial.ontology.domain.form.data.FormFieldData;
import org.industrial.ontology.domain.form.data.NumberControlData;
import org.industrial.ontology.domain.form.data.NumberControlDataDto;
import org.industrial.ontology.domain.form.data.TextControlDataDto;
import org.industrial.ontology.domain.form.field.FormFieldDescriptor;
import org.industrial.ontology.domain.form.field.NumberControlDescriptor;
import org.industrial.ontology.domain.jackson.ObjectMapperProvider;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.api.index.DataPropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Acceptance test of P0-10 (docs/05) on a project imported from pizza.owl, with a form descriptor in the current
 * {@code FormDescriptor} JSON format ({@code src/test/resources/forms/pizza-form.json}): the descriptor loads, the
 * form data of an individual is read from the project, and editing one field produces exactly the axioms that
 * replace the edited value, which {@code ChangeManager} then applies as a revision.
 * <p>
 * The {@code amino-acid-form.json} that docs/05 names is in the pre-2020 form format and cannot be read as a
 * {@code FormDescriptor}, so it is not used.
 */
public class EntityFormEndToEndIT {

    private static final String FORM_RESOURCE = "/forms/pizza-form.json";

    @TempDir
    Path dataDirectory;

    @TempDir
    Path sources;

    private ProjectKernelFixture kernel;

    private ProjectContext context;

    private ProjectFormComponents forms;

    private FormDescriptor formDescriptor;

    private OWLNamedIndividual exampleMargherita;

    @BeforeEach
    public void setUp() throws Exception {
        kernel = new ProjectKernelFixture(dataDirectory);
        context = kernel.open(kernel.importProject(PizzaOntology.copyTo(sources)));
        forms = new ProjectFormComponents(context);
        try(var in = getClass().getResourceAsStream(FORM_RESOURCE)) {
            formDescriptor = new ObjectMapperProvider().get().readerFor(FormDescriptor.class).readValue(in);
        }
        exampleMargherita = PizzaOntology.individual(context.dataFactory(), "ExampleMargherita");
    }

    @AfterEach
    public void tearDown() throws Exception {
        context.close();
        kernel.close();
    }

    @Test
    public void shouldLoadCurrentFormDescriptorFormat() {
        assertThat(formDescriptor.getFields().size(), is(3));
        assertThat(formDescriptor.getFields().get(1).getFormControlDescriptor(),
                   is(instanceOf(NumberControlDescriptor.class)));
    }

    @Test
    public void shouldReadFormDataOfIndividual() {
        var dataFactory = context.dataFactory();
        var formData = readFormData();

        var label = (TextControlDataDto) controlData(formData, 0).get(0);
        assertThat(label.getValue(), is(Optional.of(dataFactory.getOWLLiteral("Example Margherita", "en"))));
        var calories = (NumberControlDataDto) controlData(formData, 1).get(0);
        assertThat(calories.getValue(), is(Optional.of(dataFactory.getOWLLiteral(263))));
        var country = (EntityNameControlDataDto) controlData(formData, 2).get(0);
        assertThat(country.entityInternal().getEntity(), is(PizzaOntology.individual(dataFactory, "Italy")));
    }

    @Test
    public void shouldReplaceEditedValueWithExpectedAxioms() {
        var dataFactory = context.dataFactory();
        var pristine = readFormData().toFormData();
        var caloriesField = formDescriptor.getFields().get(1);
        var edited = withValue(pristine,
                               caloriesField,
                               NumberControlData.get((NumberControlDescriptor) caloriesField.getFormControlDescriptor(),
                                                     dataFactory.getOWLLiteral(300)));
        var formId = formDescriptor.getFormId();

        var generator = forms.changeListGeneratorFactory()
                             .create(exampleMargherita,
                                     ImmutableMap.of(formId, pristine),
                                     ImmutableMap.of(formId, edited));
        var result = context.changeManager().applyChanges(ProjectKernelFixture.USER, generator);

        var ontologyId = context.defaultOntologyIdManager().getDefaultOntologyId();
        var hasCalories = PizzaOntology.dataProperty(dataFactory, "hasCalorificContentValue");
        assertThat(result.getChangeList(), containsInAnyOrder(
                (OntologyChange) RemoveAxiomChange.of(ontologyId, dataFactory.getOWLDataPropertyAssertionAxiom(
                        hasCalories, exampleMargherita, dataFactory.getOWLLiteral(263))),
                AddAxiomChange.of(ontologyId, dataFactory.getOWLDataPropertyAssertionAxiom(
                        hasCalories, exampleMargherita, dataFactory.getOWLLiteral(300)))));
        assertThat(context.revisionManager().getRevisions().size(), is(2));
        assertThat(calorieValues(), contains(dataFactory.getOWLLiteral(300)));
        // Reading the form again shows the new value.
        var calories = (NumberControlDataDto) controlData(readFormData(), 1).get(0);
        assertThat(calories.getValue(), is(Optional.of(dataFactory.getOWLLiteral(300))));
    }

    @Test
    public void shouldNotChangeAnythingForUnchangedForm() {
        var pristine = readFormData().toFormData();
        var formId = formDescriptor.getFormId();

        var generator = forms.changeListGeneratorFactory()
                             .create(exampleMargherita,
                                     ImmutableMap.of(formId, pristine),
                                     ImmutableMap.of(formId, pristine));
        var result = context.changeManager().applyChanges(ProjectKernelFixture.USER, generator);

        assertThat(result.getChangeList(), is(empty()));
        assertThat(context.revisionManager().getRevisions().size(), is(1));
    }

    private FormDataDto readFormData() {
        return forms.formDataDtoBuilderFactory()
                    .create(FormRegionOrderingIndex.get(ImmutableSet.of()),
                            LangTagFilter.get(ImmutableSet.of()),
                            FormPageRequestIndex.create(ImmutableList.of()),
                            FormRegionFilterIndex.get(ImmutableSet.of()))
                    .toFormData(exampleMargherita, formDescriptor);
    }

    private List<OWLLiteral> calorieValues() {
        var hasCalories = PizzaOntology.dataProperty(context.dataFactory(), "hasCalorificContentValue");
        var ontologyId = context.defaultOntologyIdManager().getDefaultOntologyId();
        return context.indexes()
                      .get(DataPropertyAssertionAxiomsBySubjectIndex.class)
                      .getDataPropertyAssertions(exampleMargherita, ontologyId)
                      .filter(axiom -> axiom.getProperty().equals(hasCalories))
                      .map(OWLDataPropertyAssertionAxiom::getObject)
                      .collect(toList());
    }

    private static List<FormControlDataDto> controlData(FormDataDto formData, int field) {
        return formData.getFormFieldData().get(field).getFormControlData().getPageElements();
    }

    private static FormData withValue(FormData formData, FormFieldDescriptor field, FormControlData value) {
        var fieldData = formData.getFormFieldData()
                                .stream()
                                .map(data -> data.getFormFieldDescriptor().getId().equals(field.getId())
                                        ? FormFieldData.get(field, Page.of(ImmutableList.of(value)))
                                        : data)
                                .collect(ImmutableList.toImmutableList());
        return FormData.get(formData.getSubject(), formData.getFormDescriptor(), fieldData);
    }
}
