package org.industrial.ontology.kernel.index;



import javax.annotation.Nonnull;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex.AnnotationsTreatment.INCLUDE_ANNOTATIONS;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;

import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.ClassFrameAxiomsIndexImpl}.
 */
public class ClassFrameAxiomsIndex implements org.industrial.ontology.kernel.api.index.ClassFrameAxiomsIndex {

    @Nonnull
    private final ProjectOntologiesIndex ontologiesIndex;

    @Nonnull
    private final SubClassOfAxiomsBySubClassIndex subClassOfAxiomsIndex;

    @Nonnull
    private final EquivalentClassesAxiomsIndex equivalentClassesAxiomsIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsIndex;

    public ClassFrameAxiomsIndex(@Nonnull ProjectOntologiesIndex ontologiesIndex,
                                     @Nonnull SubClassOfAxiomsBySubClassIndex subClassOfAxiomsIndex,
                                     @Nonnull EquivalentClassesAxiomsIndex equivalentClassesAxiomsIndex,
                                     @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsIndex) {
        this.ontologiesIndex = checkNotNull(ontologiesIndex);
        this.subClassOfAxiomsIndex = checkNotNull(subClassOfAxiomsIndex);
        this.equivalentClassesAxiomsIndex = checkNotNull(equivalentClassesAxiomsIndex);
        this.annotationAssertionAxiomsIndex = checkNotNull(annotationAssertionAxiomsIndex);
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getFrameAxioms(@Nonnull OWLClass subject, @Nonnull AnnotationsTreatment annotationsTreatment) {
        var subClassOfAxioms = getFrameSubClassOfAxioms(subject);
        var equivalentClassesAxioms = getFrameEquivalentClassesAxioms(subject);
        var annotationAssertions = Stream.<OWLAnnotationAssertionAxiom>empty();
        if (annotationsTreatment == INCLUDE_ANNOTATIONS) {
            annotationAssertions = getFrameAnnotationAssertionsAxiom(subject);
        }
        return Stream.of(subClassOfAxioms,
            equivalentClassesAxioms,
            annotationAssertions)
            .flatMap(ax -> ax)
            .collect(toImmutableSet());
    }

    private Stream<OWLSubClassOfAxiom> getFrameSubClassOfAxioms(OWLClass subject) {
        return ontologiesIndex
            .getOntologyIds()
            .flatMap(ontId -> subClassOfAxiomsIndex.getSubClassOfAxiomsForSubClass(subject, ontId));
    }

    private Stream<OWLEquivalentClassesAxiom> getFrameEquivalentClassesAxioms(OWLClass subject) {
        return ontologiesIndex.getOntologyIds()
            .flatMap(ontId -> equivalentClassesAxiomsIndex.getEquivalentClassesAxioms(subject,
                ontId));
    }

    private Stream<OWLAnnotationAssertionAxiom> getFrameAnnotationAssertionsAxiom(OWLClass subject) {
        return ontologiesIndex.getOntologyIds()
            .flatMap(ontId -> annotationAssertionAxiomsIndex.getAxiomsForSubject(subject.getIRI(),
                ontId));
    }
}
