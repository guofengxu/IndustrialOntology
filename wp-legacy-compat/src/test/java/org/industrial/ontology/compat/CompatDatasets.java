package org.industrial.ontology.compat;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The revision histories that the compatibility tests write with the legacy revision store and replay through both
 * kernels.
 * <ul>
 *     <li>{@link #pizza}: one revision that imports pizza.owl, as the legacy {@code ProjectImporter} writes it.</li>
 *     <li>{@link #edited}: the pizza import followed by a deterministic history of edits that add, relabel, move and
 *     delete classes, add individuals, use a second ontology, and add and remove ontology annotations and imports.
 *     It stands in for a real project directory, which is not available to the tests.</li>
 *     <li>{@link #generated}: one import revision of a synthetic ontology with about five axioms per class, for the
 *     load-time baseline.</li>
 * </ul>
 */
final class CompatDatasets {

    static final String NAMESPACE = "http://www.industrial-ontology.org/test/pizza#";

    static final OWLOntologyID EXTRA_ONTOLOGY = new OWLOntologyID(
            IRI.create("http://www.industrial-ontology.org/test/pizza-extra"));

    private CompatDatasets() {
    }

    record RevisionSpec(@Nonnull String user, @Nonnull String description, @Nonnull List<NeutralChange> changes) {
    }

    static List<RevisionSpec> pizza(@Nonnull Path pizzaOwl) throws Exception {
        return List.of(importRevision(load(pizzaOwl)));
    }

    static List<RevisionSpec> edited(@Nonnull Path pizzaOwl, int edits, long seed) throws Exception {
        var pizza = load(pizzaOwl);
        var revisions = new ArrayList<RevisionSpec>();
        revisions.add(importRevision(pizza));
        new EditScript(pizza, new Random(seed)).write(edits, revisions);
        return revisions;
    }

    /**
     * One revision importing {@code classCount} classes, each with a declaration, a label, a comment, a named
     * superclass and an existential restriction.
     */
    static List<RevisionSpec> generated(int classCount) {
        var dataFactory = new OWLDataFactoryImpl();
        var ontologyId = new OWLOntologyID(IRI.create("http://www.industrial-ontology.org/test/generated"));
        var property = dataFactory.getOWLObjectProperty(IRI.create(NAMESPACE + "relatedTo"));
        var changes = new ArrayList<NeutralChange>();
        changes.add(NeutralChange.addAxiom(ontologyId, dataFactory.getOWLDeclarationAxiom(property)));
        for(int i = 0; i < classCount; i++) {
            var cls = generatedClass(dataFactory, i);
            changes.add(NeutralChange.addAxiom(ontologyId, dataFactory.getOWLDeclarationAxiom(cls)));
            changes.add(NeutralChange.addAxiom(ontologyId, label(dataFactory, cls, "Generated class " + i)));
            changes.add(NeutralChange.addAxiom(ontologyId, dataFactory.getOWLAnnotationAssertionAxiom(
                    dataFactory.getRDFSComment(), cls.getIRI(), dataFactory.getOWLLiteral("Comment " + i, "en"))));
            if(i != 0) {
                changes.add(NeutralChange.addAxiom(ontologyId, dataFactory.getOWLSubClassOfAxiom(
                        cls, generatedClass(dataFactory, i / 8))));
            }
            changes.add(NeutralChange.addAxiom(ontologyId, dataFactory.getOWLSubClassOfAxiom(
                    cls,
                    dataFactory.getOWLObjectSomeValuesFrom(property,
                                                           generatedClass(dataFactory, (i * 7 + 3) % classCount)))));
        }
        return List.of(new RevisionSpec("importer", "Initial import", changes));
    }

    private static OWLClass generatedClass(OWLDataFactory dataFactory, int i) {
        return dataFactory.getOWLClass(IRI.create("http://www.industrial-ontology.org/test/generated#C" + i));
    }

    private static OWLOntology load(Path document) throws Exception {
        return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(document.toFile());
    }

    /**
     * The changes of the legacy {@code ProjectImporter}: axioms, then ontology annotations, then imports.
     */
    private static RevisionSpec importRevision(OWLOntology ontology) {
        var ontologyId = ontology.getOntologyID();
        var changes = new ArrayList<NeutralChange>();
        sorted(ontology.getAxioms()).forEach(axiom -> changes.add(NeutralChange.addAxiom(ontologyId, axiom)));
        sorted(ontology.getAnnotations()).forEach(annotation -> changes.add(
                NeutralChange.addAnnotation(ontologyId, annotation)));
        sorted(ontology.getImportsDeclarations()).forEach(declaration -> changes.add(
                NeutralChange.addImport(ontologyId, declaration)));
        return new RevisionSpec("importer", "Initial import", changes);
    }

    private static OWLAnnotationAssertionAxiom label(OWLDataFactory dataFactory, OWLClass cls, String label) {
        return dataFactory.getOWLAnnotationAssertionAxiom(dataFactory.getRDFSLabel(),
                                                          cls.getIRI(),
                                                          dataFactory.getOWLLiteral(label, "en"));
    }

    private static <T> List<T> sorted(Collection<T> objects) {
        return objects.stream().sorted(Comparator.comparing(Object::toString)).collect(Collectors.toList());
    }

    /**
     * Generates edits from the current state of each ontology, so removals only remove what is there.
     */
    private static final class EditScript {

        private static final List<String> USERS = List.of("alice", "bob", "chen");

        private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

        private final OWLOntologyID mainOntology;

        private final Random random;

        private final Map<OWLOntologyID, Set<OWLAxiom>> axioms = new HashMap<>();

        private final List<OWLAnnotation> addedAnnotations = new ArrayList<>();

        private final List<OWLClass> editClasses = new ArrayList<>();

        private boolean extraImported = false;

        private EditScript(OWLOntology pizza, Random random) {
            this.mainOntology = pizza.getOntologyID();
            this.random = random;
            axioms.put(mainOntology, new LinkedHashSet<>(pizza.getAxioms()));
            axioms.put(EXTRA_ONTOLOGY, new LinkedHashSet<>());
        }

        void write(int edits, List<RevisionSpec> revisions) {
            for(int i = 1; i <= edits; i++) {
                var changes = new ArrayList<NeutralChange>();
                switch(i % 9) {
                    case 0 -> deleteEditClass(changes);
                    case 1 -> createClass(i, changes);
                    case 2 -> removeSubClassOf(changes);
                    case 3 -> relabel(i, changes);
                    case 4 -> addToExtraOntology(i, changes);
                    case 5 -> addOntologyAnnotation(i, changes);
                    case 6 -> removeOntologyAnnotation(i, changes);
                    case 7 -> addIndividual(i, changes);
                    default -> toggleImport(changes);
                }
                if(!changes.isEmpty()) {
                    revisions.add(new RevisionSpec(USERS.get(i % USERS.size()), "Edit " + i, changes));
                }
            }
        }

        private void createClass(int i, List<NeutralChange> changes) {
            var cls = dataFactory.getOWLClass(IRI.create(NAMESPACE + "EditClass" + i));
            var parent = pick(classesIn(mainOntology));
            add(mainOntology, dataFactory.getOWLDeclarationAxiom(cls), changes);
            add(mainOntology, label(dataFactory, cls, "Edit Class " + i), changes);
            add(mainOntology, dataFactory.getOWLSubClassOfAxiom(cls, parent), changes);
            editClasses.add(cls);
        }

        private void deleteEditClass(List<NeutralChange> changes) {
            if(editClasses.isEmpty()) {
                return;
            }
            var cls = editClasses.remove(random.nextInt(editClasses.size()));
            for(var ontologyId : List.of(mainOntology, EXTRA_ONTOLOGY)) {
                var referencing = axioms.get(ontologyId)
                                        .stream()
                                        .filter(axiom -> axiom.getSignature().contains(cls)
                                                || isAnnotationOf(axiom, cls.getIRI()))
                                        .collect(Collectors.toList());
                sorted(referencing).forEach(axiom -> remove(ontologyId, axiom, changes));
            }
        }

        private void removeSubClassOf(List<NeutralChange> changes) {
            var candidates = axioms.get(mainOntology)
                                   .stream()
                                   .filter(axiom -> axiom.isOfType(AxiomType.SUBCLASS_OF))
                                   .filter(axiom -> !((OWLSubClassOfAxiom) axiom).getSuperClass().isAnonymous())
                                   .collect(Collectors.toList());
            if(!candidates.isEmpty()) {
                remove(mainOntology, pick(candidates), changes);
            }
        }

        private void relabel(int i, List<NeutralChange> changes) {
            var labels = axioms.get(mainOntology)
                               .stream()
                               .filter(axiom -> axiom.isOfType(AxiomType.ANNOTATION_ASSERTION))
                               .map(axiom -> (OWLAnnotationAssertionAxiom) axiom)
                               .filter(axiom -> axiom.getProperty().isLabel())
                               .collect(Collectors.toList());
            var old = pick(labels);
            var oldLiteral = (OWLLiteral) old.getValue();
            remove(mainOntology, old, changes);
            add(mainOntology,
                dataFactory.getOWLAnnotationAssertionAxiom(old.getProperty(),
                                                           old.getSubject(),
                                                           dataFactory.getOWLLiteral(
                                                                   oldLiteral.getLiteral() + " v" + i,
                                                                   oldLiteral.getLang())),
                changes);
        }

        private void addToExtraOntology(int i, List<NeutralChange> changes) {
            var cls = dataFactory.getOWLClass(IRI.create("http://www.industrial-ontology.org/test/pizza-extra#X" + i));
            add(EXTRA_ONTOLOGY, dataFactory.getOWLDeclarationAxiom(cls), changes);
            add(EXTRA_ONTOLOGY, label(dataFactory, cls, "Extra " + i), changes);
            add(EXTRA_ONTOLOGY,
                dataFactory.getOWLSubClassOfAxiom(cls, dataFactory.getOWLClass(IRI.create(NAMESPACE + "Pizza"))),
                changes);
        }

        private void addOntologyAnnotation(int i, List<NeutralChange> changes) {
            var annotation = dataFactory.getOWLAnnotation(dataFactory.getRDFSComment(),
                                                          dataFactory.getOWLLiteral("Edit note " + i, "en"));
            addedAnnotations.add(annotation);
            changes.add(NeutralChange.addAnnotation(mainOntology, annotation));
        }

        private void removeOntologyAnnotation(int i, List<NeutralChange> changes) {
            if(addedAnnotations.size() > 1) {
                changes.add(NeutralChange.removeAnnotation(mainOntology, addedAnnotations.remove(0)));
            }
            else {
                addOntologyAnnotation(i, changes);
            }
        }

        private void addIndividual(int i, List<NeutralChange> changes) {
            var individual = dataFactory.getOWLNamedIndividual(IRI.create(NAMESPACE + "Ind" + i));
            var type = dataFactory.getOWLClass(IRI.create(NAMESPACE + (i % 2 == 0 ? "Margherita" : "AmericanHot")));
            add(mainOntology, dataFactory.getOWLDeclarationAxiom(individual), changes);
            add(mainOntology, dataFactory.getOWLClassAssertionAxiom(type, individual), changes);
            add(mainOntology,
                dataFactory.getOWLDataPropertyAssertionAxiom(
                        dataFactory.getOWLDataProperty(IRI.create(NAMESPACE + "hasCalorificContentValue")),
                        individual,
                        dataFactory.getOWLLiteral(200 + i)),
                changes);
        }

        private void toggleImport(List<NeutralChange> changes) {
            var declaration = dataFactory.getOWLImportsDeclaration(EXTRA_ONTOLOGY.getOntologyIRI().get());
            changes.add(extraImported
                                ? NeutralChange.removeImport(mainOntology, declaration)
                                : NeutralChange.addImport(mainOntology, declaration));
            extraImported = !extraImported;
        }

        private List<OWLClass> classesIn(OWLOntologyID ontologyId) {
            return axioms.get(ontologyId)
                         .stream()
                         .filter(axiom -> axiom.isOfType(AxiomType.DECLARATION))
                         .flatMap(axiom -> axiom.getClassesInSignature().stream())
                         .distinct()
                         .collect(Collectors.toList());
        }

        private static boolean isAnnotationOf(OWLAxiom axiom, IRI subject) {
            return axiom instanceof OWLAnnotationAssertionAxiom annotation && annotation.getSubject().equals(subject);
        }

        private <T> T pick(Collection<T> candidates) {
            var ordered = sorted(candidates);
            return ordered.get(random.nextInt(ordered.size()));
        }

        private void add(OWLOntologyID ontologyId, OWLAxiom axiom, List<NeutralChange> changes) {
            if(axioms.get(ontologyId).add(axiom)) {
                changes.add(NeutralChange.addAxiom(ontologyId, axiom));
            }
        }

        private void remove(OWLOntologyID ontologyId, OWLAxiom axiom, List<NeutralChange> changes) {
            if(axioms.get(ontologyId).remove(axiom)) {
                changes.add(NeutralChange.removeAxiom(ontologyId, axiom));
            }
        }
    }
}
