package org.industrial.ontology.kernel.hierarchy;

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
import java.util.function.Function;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;

/**
 * Acceptance test of P0-06 (docs/05) on a project imported from pizza.owl: the four hierarchy providers of the
 * project context see the roots and children the ontology asserts, including the parents that defined classes get
 * from their {@code EquivalentClasses} intersections. {@code ClassHierarchyCompatibilityIT} in wp-legacy-compat
 * compares every class with the legacy provider.
 */
public class PizzaClassHierarchyIT {

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
    public void shouldBuildClassHierarchy() {
        var classes = context.hierarchies().classHierarchy();
        var thing = context.dataFactory().getOWLThing();
        assertThat(classes.getRoots(), contains(thing));
        assertThat(classes.getChildren(thing), containsInAnyOrder(classes("DomainConcept", "ValuePartition")));
        assertThat(classes.getChildren(cls("DomainConcept")), containsInAnyOrder(classes("Food", "Kitchen", "Country")));
        assertThat(classes.getChildren(cls("Pizza")), containsInAnyOrder(classes("NamedPizza",
                                                                                  "CheesyPizza",
                                                                                  "VegetarianPizza",
                                                                                  "MeatyPizza",
                                                                                  "SpicyPizza",
                                                                                  "InterestingPizza",
                                                                                  "RealItalianPizza",
                                                                                  "ThinAndCrispyPizza")));
        assertThat(classes.getChildren(cls("NamedPizza")), containsInAnyOrder(classes("Margherita",
                                                                                       "Napoletana",
                                                                                       "Marinara",
                                                                                       "American",
                                                                                       "AmericanHot",
                                                                                       "QuattroFormaggi",
                                                                                       "Capricciosa",
                                                                                       "Funghi",
                                                                                       "Siciliana",
                                                                                       "Diavola",
                                                                                       "Veneziana")));
        assertThat(classes.getChildren(cls("PizzaTopping")), containsInAnyOrder(classes("CheeseTopping",
                                                                                         "MeatTopping",
                                                                                         "FishTopping",
                                                                                         "VegetableTopping",
                                                                                         "HerbSpiceTopping")));
        assertThat(classes.getChildren(cls("Spiciness")), containsInAnyOrder(classes("Hot", "Medium", "Mild")));
        assertThat(classes.isLeaf(cls("JalapenoPepperTopping")), is(true));
        assertThat(classes.getPathsToRoot(cls("JalapenoPepperTopping")),
                   contains(List.of(context.dataFactory().getOWLThing(),
                                    cls("DomainConcept"),
                                    cls("Food"),
                                    cls("PizzaTopping"),
                                    cls("VegetableTopping"),
                                    cls("PepperTopping"),
                                    cls("JalapenoPepperTopping"))));
    }

    @Test
    public void shouldBuildPropertyHierarchies() {
        var dataFactory = context.dataFactory();
        var objectProperties = context.hierarchies().objectPropertyHierarchy();
        assertThat(objectProperties.getChildren(dataFactory.getOWLTopObjectProperty()),
                   containsInAnyOrder(entities(name -> PizzaOntology.objectProperty(dataFactory, name),
                                               "hasIngredient",
                                               "isIngredientOf",
                                               "hasSpiciness",
                                               "hasCountryOfOrigin")));
        assertThat(objectProperties.getChildren(PizzaOntology.objectProperty(dataFactory, "hasIngredient")),
                   containsInAnyOrder(entities(name -> PizzaOntology.objectProperty(dataFactory, name),
                                               "hasTopping",
                                               "hasBase")));

        var dataProperties = context.hierarchies().dataPropertyHierarchy();
        assertThat(dataProperties.getChildren(dataFactory.getOWLTopDataProperty()),
                   containsInAnyOrder(entities(name -> PizzaOntology.dataProperty(dataFactory, name),
                                               "hasCalorificContentValue",
                                               "hasDiameterInCentimetres")));

        var annotationProperties = context.hierarchies().annotationPropertyHierarchy();
        assertThat(annotationProperties.getParents(
                           dataFactory.getOWLAnnotationProperty(PizzaOntology.iri("pizzaNote"))),
                   contains(dataFactory.getRDFSComment()));
    }

    private OWLClass cls(String localName) {
        return PizzaOntology.cls(context.dataFactory(), localName);
    }

    private OWLClass[] classes(String... localNames) {
        return Stream.of(localNames).map(this::cls).toArray(OWLClass[]::new);
    }

    private static OWLEntity[] entities(Function<String, OWLEntity> entity, String... localNames) {
        return Stream.of(localNames).map(entity).toArray(OWLEntity[]::new);
    }
}
