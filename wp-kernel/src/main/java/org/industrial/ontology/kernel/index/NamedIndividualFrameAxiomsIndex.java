package org.industrial.ontology.kernel.index;



import com.google.common.collect.Streams;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import javax.annotation.Nonnull;

import java.util.Set;
import java.util.stream.Stream;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.industrial.ontology.kernel.api.index.ClassAssertionAxiomsByIndividualIndex;

import org.industrial.ontology.kernel.api.index.NamedIndividualFrameAxiomIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.PropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.SameIndividualAxiomsIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.NamedIndividualFrameAxiomsIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-06
 */
public class NamedIndividualFrameAxiomsIndex implements NamedIndividualFrameAxiomIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final ClassAssertionAxiomsByIndividualIndex classAssertionsByIndividual;

    @Nonnull
    private final PropertyAssertionAxiomsBySubjectIndex assertionsBySubject;

    @Nonnull
    private final SameIndividualAxiomsIndex sameIndividualAxiomsIndex;

    public NamedIndividualFrameAxiomsIndex(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                               @Nonnull ClassAssertionAxiomsByIndividualIndex classAssertionsByIndividual,
                                               @Nonnull PropertyAssertionAxiomsBySubjectIndex assertionsBySubject,
                                               @Nonnull SameIndividualAxiomsIndex sameIndividualAxiomsIndex) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.classAssertionsByIndividual = classAssertionsByIndividual;
        this.assertionsBySubject = assertionsBySubject;
        this.sameIndividualAxiomsIndex = sameIndividualAxiomsIndex;
    }

    @Nonnull
    @Override
    public Set<OWLAxiom> getNamedIndividualFrameAxioms(@Nonnull OWLNamedIndividual subject) {
        var classAssertions = getClassAssertionAxioms(subject);
        var propertyAssertions = getPropertyAssertionAxioms(subject);
        var sameIndividualAxioms = getSameIndividualAxioms(subject);
        return Streams.concat(classAssertions,
                              propertyAssertions,
                              sameIndividualAxioms)
                      .collect(toImmutableSet());
    }

    private Stream<OWLAxiom> getPropertyAssertionAxioms(OWLNamedIndividual subject) {
        return projectOntologiesIndex
                .getOntologyIds()
                .flatMap(ontId -> assertionsBySubject.getPropertyAssertions(subject, ontId));
    }

    private Stream<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLNamedIndividual subjectIndividual) {
        return projectOntologiesIndex
                .getOntologyIds()
                .flatMap(ontId -> classAssertionsByIndividual.getClassAssertionAxioms(subjectIndividual, ontId));
    }

    private Stream<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLNamedIndividual subjectIndividual) {
        return projectOntologiesIndex
                .getOntologyIds()
                .flatMap(ontId -> sameIndividualAxiomsIndex.getSameIndividualAxioms(subjectIndividual, ontId));
    }

}
