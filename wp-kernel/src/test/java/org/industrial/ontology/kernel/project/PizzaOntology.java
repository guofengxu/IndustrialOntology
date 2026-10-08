package org.industrial.ontology.kernel.project;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The pizza test ontology in {@code src/test/resources/ontologies/pizza/pizza.owl}: 60 classes and 377 axioms in
 * RDF/XML, with English and Chinese labels, defined classes, a deprecated class and individuals (see the README next
 * to it). The acceptance tests of docs/05 P0-06, P0-07 and P0-09 run on it.
 */
public final class PizzaOntology {

    public static final String RESOURCE = "/ontologies/pizza/pizza.owl";

    public static final String NAMESPACE = "http://www.industrial-ontology.org/test/pizza#";

    public static final IRI ONTOLOGY_IRI = IRI.create("http://www.industrial-ontology.org/test/pizza");

    public static final int AXIOM_COUNT = 377;

    public static final int ONTOLOGY_ANNOTATION_COUNT = 4;

    private PizzaOntology() {
    }

    /**
     * Copies pizza.owl into {@code directory}.
     */
    @Nonnull
    public static Path copyTo(@Nonnull Path directory) throws IOException {
        return ProjectKernelFixture.copyResource(RESOURCE, directory);
    }

    @Nonnull
    public static IRI iri(@Nonnull String localName) {
        return IRI.create(NAMESPACE + localName);
    }

    @Nonnull
    public static OWLClass cls(@Nonnull OWLDataFactory dataFactory, @Nonnull String localName) {
        return dataFactory.getOWLClass(iri(localName));
    }

    @Nonnull
    public static OWLObjectProperty objectProperty(@Nonnull OWLDataFactory dataFactory, @Nonnull String localName) {
        return dataFactory.getOWLObjectProperty(iri(localName));
    }

    @Nonnull
    public static OWLDataProperty dataProperty(@Nonnull OWLDataFactory dataFactory, @Nonnull String localName) {
        return dataFactory.getOWLDataProperty(iri(localName));
    }

    @Nonnull
    public static OWLNamedIndividual individual(@Nonnull OWLDataFactory dataFactory, @Nonnull String localName) {
        return dataFactory.getOWLNamedIndividual(iri(localName));
    }
}
