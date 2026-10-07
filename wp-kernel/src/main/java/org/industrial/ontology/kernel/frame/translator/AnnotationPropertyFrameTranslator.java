package org.industrial.ontology.kernel.frame.translator;



import org.industrial.ontology.kernel.frame.Mode;
import org.industrial.ontology.domain.core.DataFactory;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import java.util.function.Function;
import java.util.function.Predicate;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.AnnotationPropertyRangeAxiomsIndex;

import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.AnnotationPropertyFrameTranslator}.
 * <p>
 * Author: Matthew Horridge<br> Stanford University<br> Bio-Medical Informatics Research Group<br> Date: 23/04/2013
 */
public class AnnotationPropertyFrameTranslator {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    @Nonnull
    private final AnnotationPropertyDomainAxiomsIndex domainAxiomsIndex;

    @Nonnull
    private final AnnotationPropertyRangeAxiomsIndex rangeAxiomsIndex;

    public AnnotationPropertyFrameTranslator(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                             @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex,
                                             @Nonnull AnnotationPropertyDomainAxiomsIndex domainAxiomsIndex,
                                             @Nonnull AnnotationPropertyRangeAxiomsIndex rangeAxiomsIndex) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.annotationAssertionsIndex = annotationAssertionsIndex;
        this.domainAxiomsIndex = domainAxiomsIndex;
        this.rangeAxiomsIndex = rangeAxiomsIndex;
    }

    @Nonnull
    public PlainAnnotationPropertyFrame getFrame(@Nonnull OWLAnnotationProperty subject) {
        var propertyIRI = subject.getIRI();
        var propertyValues = projectOntologiesIndex.getOntologyIds()
                .flatMap(ontId -> annotationAssertionsIndex.getAxiomsForSubject(propertyIRI, ontId))
                .filter(isTranslatableValue())
                .distinct()
                .map(toPropertyValue())
                .collect(toImmutableSet());

        var domains = projectOntologiesIndex.getOntologyIds()
                .flatMap(ontId -> domainAxiomsIndex.getAnnotationPropertyDomainAxioms(subject, ontId))
                .map(OWLAnnotationPropertyDomainAxiom::getDomain)
                .distinct()
                .sorted()
                .collect(toImmutableSet());

        var ranges = projectOntologiesIndex.getOntologyIds()
                .flatMap(ontId -> rangeAxiomsIndex.getAnnotationPropertyRangeAxioms(subject, ontId))
                .map(OWLAnnotationPropertyRangeAxiom::getRange)
                .distinct()
                .sorted()
                .collect(toImmutableSet());

        return PlainAnnotationPropertyFrame.get(subject,
                                           propertyValues,
                                           domains,
                                           ranges);
    }

    /**
     * Determines whether the annotation value is translatable (must be a literal or IRI)
     */
    private Predicate<OWLAnnotationAssertionAxiom> isTranslatableValue() {
        return ax -> !(ax.getValue() instanceof OWLAnonymousIndividual);
    }

    /**
     * Converts annotation assertion axioms to property values
     */
    private Function<OWLAnnotationAssertionAxiom, PlainPropertyAnnotationValue> toPropertyValue() {
        return ax -> PlainPropertyAnnotationValue.get(
               ax.getProperty(),
                ax.getValue(),
                State.ASSERTED
        );
    }

    @Nonnull
    public Set<OWLAxiom> getAxioms(@Nonnull PlainAnnotationPropertyFrame frame, @Nonnull Mode mode) {
        Set<OWLAxiom> result = new HashSet<>();
        for (PlainPropertyAnnotationValue value : frame.getPropertyValues()) {
            result.add(DataFactory.get().getOWLAnnotationAssertionAxiom(value.getProperty(), frame.getSubject().getIRI(),
                                                                                             value.getValue()));
        }
        for (IRI domain : frame.getDomains()) {
            result.add(DataFactory.get().getOWLAnnotationPropertyDomainAxiom(frame.getSubject(), domain));
        }
        for (IRI range : frame.getRanges()) {
            result.add(DataFactory.get().getOWLAnnotationPropertyRangeAxiom(frame.getSubject(), range));
        }
        return result;
    }
}
