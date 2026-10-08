package org.industrial.ontology.compat;

import edu.stanford.bmir.protege.web.server.hierarchy.ClassHierarchyProviderImpl;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex;
import org.industrial.ontology.kernel.api.index.OntologyAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Runs the same queries against the legacy kernel and the ported one, records every query whose results differ and
 * writes them to a report under {@code target/compat-reports} (docs/05 P0-05: 差异输出到报告文件).
 */
final class KernelComparison {

    private static final Path REPORTS = Path.of("target", "compat-reports");

    private final List<String> differences = new ArrayList<>();

    private int comparisons = 0;

    /**
     * Compares the four indexes named by P0-05 for every ontology and every signature entity, plus the ontology ids
     * and the project signature that drive them.
     */
    void compareIndexes(@Nonnull LegacyKernel legacy, @Nonnull ProjectContext ported) {
        var indexes = ported.indexes();
        var ontologyIds = union("ProjectOntologiesIndex.getOntologyIds()",
                                legacy.projectOntologies.getOntologyIds(),
                                indexes.get(ProjectOntologiesIndex.class).getOntologyIds());
        var signature = union("ProjectSignatureIndex.getSignature()",
                              legacy.projectSignature.getSignature(),
                              indexes.get(ProjectSignatureIndex.class).getSignature());
        var ontologyAxioms = indexes.get(OntologyAxiomsIndex.class);
        var classFrames = indexes.get(ClassFrameAxiomsIndex.class);
        var subClassOf = indexes.get(SubClassOfAxiomsBySubClassIndex.class);
        var annotationAssertions = indexes.get(AnnotationAssertionAxiomsBySubjectIndex.class);
        for(var ontologyId : ontologyIds) {
            compare("OntologyAxiomsIndex.getAxioms(" + ontologyId + ")",
                    legacy.ontologyAxioms.getAxioms(ontologyId),
                    ontologyAxioms.getAxioms(ontologyId));
        }
        for(var entity : signature) {
            if(entity.isOWLClass()) {
                var cls = entity.asOWLClass();
                compare("ClassFrameAxiomsIndex.getFrameAxioms(" + cls + ", INCLUDE_ANNOTATIONS)",
                        legacy.classFrameAxioms.getFrameAxioms(cls, edu.stanford.bmir.protege.web.server.index
                                .ClassFrameAxiomsIndex.AnnotationsTreatment.INCLUDE_ANNOTATIONS),
                        classFrames.getFrameAxioms(cls, ClassFrameAxiomsIndex.AnnotationsTreatment.INCLUDE_ANNOTATIONS));
                compare("ClassFrameAxiomsIndex.getFrameAxioms(" + cls + ", EXCLUDE_ANNOTATIONS)",
                        legacy.classFrameAxioms.getFrameAxioms(cls, edu.stanford.bmir.protege.web.server.index
                                .ClassFrameAxiomsIndex.AnnotationsTreatment.EXCLUDE_ANNOTATIONS),
                        classFrames.getFrameAxioms(cls, ClassFrameAxiomsIndex.AnnotationsTreatment.EXCLUDE_ANNOTATIONS));
                for(var ontologyId : ontologyIds) {
                    compare("SubClassOfAxiomsBySubClassIndex.getSubClassOfAxiomsForSubClass(" + cls + ", "
                                    + ontologyId + ")",
                            legacy.subClassOfAxioms.getSubClassOfAxiomsForSubClass(cls, ontologyId),
                            subClassOf.getSubClassOfAxiomsForSubClass(cls, ontologyId));
                }
            }
            for(var ontologyId : ontologyIds) {
                compare("AnnotationAssertionAxiomsBySubjectIndex.getAxiomsForSubject(" + entity.getIRI() + ", "
                                + ontologyId + ")",
                        legacy.annotationAssertionsBySubject.getAxiomsForSubject(entity.getIRI(), ontologyId),
                        annotationAssertions.getAxiomsForSubject(entity.getIRI(), ontologyId));
            }
        }
    }

    /**
     * Compares roots, children, parents, ancestors, leaf status and paths to the root of every class.
     */
    void compareClassHierarchies(@Nonnull ClassHierarchyProviderImpl legacy,
                                 @Nonnull ClassHierarchyProvider ported,
                                 @Nonnull Collection<OWLClass> classes) {
        compare("getRoots()", legacy.getRoots(), ported.getRoots());
        for(var cls : classes) {
            compare("getChildren(" + cls + ")", legacy.getChildren(cls), ported.getChildren(cls));
            compare("getParents(" + cls + ")", legacy.getParents(cls), ported.getParents(cls));
            compare("getAncestors(" + cls + ")", legacy.getAncestors(cls), ported.getAncestors(cls));
            compare("getPathsToRoot(" + cls + ")", legacy.getPathsToRoot(cls), ported.getPathsToRoot(cls));
            compare("isLeaf(" + cls + ")", List.of(legacy.isLeaf(cls)), List.of(ported.isLeaf(cls)));
        }
    }

    /**
     * The classes in the signature of the ported project, with {@code owl:Thing}.
     */
    static List<OWLClass> classesOf(@Nonnull ProjectContext ported) {
        var classes = new LinkedHashSet<OWLClass>();
        classes.add(ported.dataFactory().getOWLThing());
        ported.indexes()
              .get(ProjectSignatureIndex.class)
              .getSignature()
              .filter(OWLEntity::isOWLClass)
              .map(OWLEntity::asOWLClass)
              .sorted()
              .forEach(classes::add);
        return new ArrayList<>(classes);
    }

    int comparisons() {
        return comparisons;
    }

    List<String> differences() {
        return differences;
    }

    /**
     * Writes the report and returns its path.
     */
    Path writeReport(@Nonnull String name, @Nonnull String title) throws IOException {
        Files.createDirectories(REPORTS);
        var lines = new ArrayList<String>();
        lines.add(title);
        lines.add("Comparisons: " + comparisons + ", differences: " + differences.size());
        lines.addAll(differences);
        var report = REPORTS.resolve(name + ".txt");
        Files.write(report, lines, StandardCharsets.UTF_8);
        return report;
    }

    private void compare(String query, Stream<?> legacy, Stream<?> ported) {
        compare(query, legacy.collect(Collectors.toList()), ported.collect(Collectors.toList()));
    }

    private void compare(String query, Collection<?> legacy, Collection<?> ported) {
        comparisons++;
        var legacySet = new HashSet<Object>(legacy);
        var portedSet = new HashSet<Object>(ported);
        if(legacySet.equals(portedSet)) {
            return;
        }
        differences.add(query
                                + "\n    only legacy: " + sortedDifference(legacySet, portedSet)
                                + "\n    only ported: " + sortedDifference(portedSet, legacySet));
    }

    private <T extends Comparable<? super T>> List<T> union(String query, Stream<T> legacy, Stream<T> ported) {
        var legacyItems = legacy.collect(Collectors.toSet());
        var portedItems = ported.collect(Collectors.toSet());
        compare(query, legacyItems, portedItems);
        var union = new HashSet<T>(legacyItems);
        union.addAll(portedItems);
        return union.stream().sorted().collect(Collectors.toList());
    }

    private static List<String> sortedDifference(Set<Object> from, Set<Object> minus) {
        return from.stream()
                   .filter(item -> !minus.contains(item))
                   .map(String::valueOf)
                   .sorted()
                   .collect(Collectors.toList());
    }
}
