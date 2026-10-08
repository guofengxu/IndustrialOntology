package org.industrial.ontology.kernel.lucene;

import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.change.FixedChangeListGenerator;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

/**
 * Acceptance test of P0-07 (docs/05) on a project imported from pizza.owl: the Lucene dictionary built when the
 * project is loaded finds the pizza classes by prefix, a label written afterwards is searchable at once, and the
 * Lucene-backed deprecated-entity index sees {@code owl:deprecated}.
 */
public class PizzaSearchIT {

    @TempDir
    Path dataDirectory;

    @TempDir
    Path sources;

    private ProjectKernelFixture kernel;

    private ProjectContext context;

    @BeforeEach
    public void setUp() throws Exception {
        kernel = new ProjectKernelFixture(dataDirectory);
        context = kernel.open(kernel.importProject(PizzaOntology.copyTo(sources)));
    }

    @AfterEach
    public void tearDown() throws Exception {
        context.close();
        kernel.close();
    }

    @Test
    public void shouldFindPizzaClassesByPrefix() {
        assertThat(search("piz"), containsInAnyOrder(classes("Pizza",
                                                             "NamedPizza",
                                                             "CheesyPizza",
                                                             "VegetarianPizza",
                                                             "MeatyPizza",
                                                             "SpicyPizza",
                                                             "InterestingPizza",
                                                             "RealItalianPizza",
                                                             "ThinAndCrispyPizza",
                                                             "PizzaBase",
                                                             "PizzaTopping")));
        assertThat(search("mozz"), contains(cls("MozzarellaTopping")));
        assertThat(search("cheese topp"), contains(cls("CheeseTopping")));
        assertThat(search("zzz"), is(empty()));
    }

    @Test
    public void shouldRenderShortFormsPerLanguage() {
        var pizza = cls("Pizza");
        assertThat(context.dictionary().getShortForm(pizza, List.of(DictionaryLanguage.rdfsLabel("zh"))), is("比萨"));
        assertThat(context.dictionary().getShortForm(pizza, List.of(DictionaryLanguage.rdfsLabel("en"))),
                   is("Pizza"));
    }

    @Test
    public void shouldSearchNewLabelImmediately() {
        var napoletana = cls("Napoletana");
        assertThat(search("classica"), is(empty()));

        var label = context.dataFactory().getOWLAnnotationAssertionAxiom(
                context.dataFactory().getRDFSLabel(),
                napoletana.getIRI(),
                context.dataFactory().getOWLLiteral("Pizza Napoletana Classica", "en"));
        var ontologyId = context.defaultOntologyIdManager().getDefaultOntologyId();
        context.changeManager().applyChanges(ProjectKernelFixture.USER,
                                             FixedChangeListGenerator.get(List.of(AddAxiomChange.of(ontologyId,
                                                                                                    label)),
                                                                          napoletana,
                                                                          "Added a label"));

        assertThat(search("classica"), contains(napoletana));
        assertThat(search("piz"), hasItem(napoletana));
    }

    @Test
    public void shouldReadDeprecationFromLuceneIndex() {
        assertThat(context.deprecatedEntityChecker().isDeprecated(cls("Veneziana")), is(true));
        assertThat(context.deprecatedEntityChecker().isDeprecated(cls("Margherita")), is(false));
    }

    private List<OWLEntity> search(String text) {
        return ProjectKernelFixture.searchClasses(context, text);
    }

    private OWLClass cls(String localName) {
        return PizzaOntology.cls(context.dataFactory(), localName);
    }

    private OWLEntity[] classes(String... localNames) {
        return Stream.of(localNames).map(this::cls).collect(toList()).toArray(new OWLEntity[0]);
    }
}
