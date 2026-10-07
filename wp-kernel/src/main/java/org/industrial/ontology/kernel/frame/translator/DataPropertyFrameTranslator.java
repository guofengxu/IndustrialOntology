package org.industrial.ontology.kernel.frame.translator;

import org.industrial.ontology.kernel.frame.Mode;
import org.industrial.ontology.domain.core.DataFactory;
import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyCharacteristicsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyDomainAxiomsIndex;
import org.industrial.ontology.kernel.api.index.DataPropertyRangeAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.frame.translator.DataPropertyFrameTranslator}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/04/2013
 */
public class DataPropertyFrameTranslator {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    @Nonnull
    private final DataPropertyDomainAxiomsIndex domainAxiomsIndex;

    @Nonnull
    private final DataPropertyRangeAxiomsIndex rangeAxiomsIndex;

    @Nonnull
    private final DataPropertyCharacteristicsIndex characteristicsIndex;

    @Nonnull
    private final Supplier<AxiomPropertyValueTranslator> axiomPropertyValueTranslatorProvider;

    @Nonnull
    private final PropertyValue2AxiomTranslator propertyValue2AxiomTranslator;

    public DataPropertyFrameTranslator(@Nonnull ProjectOntologiesIndex projectOntologiesIndex, @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionAxiomsBySubjectId, @Nonnull DataPropertyDomainAxiomsIndex dataPropertyDomainAxiomsIndex, @Nonnull DataPropertyRangeAxiomsIndex dataPropertyRangeAxiomsIndex, @Nonnull DataPropertyCharacteristicsIndex dataPropertyCharacteristicsIndex, @Nonnull Supplier<AxiomPropertyValueTranslator> axiomPropertyValueTranslatorProvider, @Nonnull PropertyValue2AxiomTranslator propertyValue2AxiomTranslator) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.annotationAssertionsIndex = annotationAssertionAxiomsBySubjectId;
        this.domainAxiomsIndex = dataPropertyDomainAxiomsIndex;
        this.rangeAxiomsIndex = dataPropertyRangeAxiomsIndex;
        this.characteristicsIndex = dataPropertyCharacteristicsIndex;
        this.axiomPropertyValueTranslatorProvider = axiomPropertyValueTranslatorProvider;
        this.propertyValue2AxiomTranslator = propertyValue2AxiomTranslator;
    }

    @Nonnull
    public PlainDataPropertyFrame getFrame(@Nonnull OWLDataProperty subject) {
        var propertyValueAxioms = projectOntologiesIndex.getOntologyIds().flatMap(ontId -> annotationAssertionsIndex.getAxiomsForSubject(subject.getIRI(), ontId)).collect(toImmutableSet());
        var domains = projectOntologiesIndex.getOntologyIds().flatMap(ontId -> domainAxiomsIndex.getDataPropertyDomainAxioms(subject, ontId)).map(OWLDataPropertyDomainAxiom::getDomain).filter(OWLClassExpression::isNamed).map(OWLClassExpression::asOWLClass).collect(toImmutableSet());
        var ranges = projectOntologiesIndex.getOntologyIds().flatMap(ontId -> rangeAxiomsIndex.getDataPropertyRangeAxioms(subject, ontId)).map(OWLDataPropertyRangeAxiom::getRange).filter(OWLDataRange::isDatatype).map(OWLDataRange::asOWLDatatype).collect(toImmutableSet());
        var functional = projectOntologiesIndex.getOntologyIds().anyMatch(ont -> characteristicsIndex.isFunctional(subject, ont));
        AxiomPropertyValueTranslator translator = axiomPropertyValueTranslatorProvider.get();
        var propertyValues = propertyValueAxioms.stream().flatMap(ax -> translator.getPropertyValues(subject, ax, State.ASSERTED).stream()).filter(PlainPropertyValue::isAnnotation).map(pv -> (PlainPropertyAnnotationValue) pv).collect(toImmutableSet());
        return PlainDataPropertyFrame.get(subject, propertyValues, domains, ranges, functional);
    }

    @Nonnull
    public Set<OWLAxiom> getAxioms(@Nonnull PlainDataPropertyFrame frame, @Nonnull Mode mode) {
        Set<OWLAxiom> result = new HashSet<>();
        for (PlainPropertyAnnotationValue pv : frame.getPropertyValues()) {
            result.addAll(propertyValue2AxiomTranslator.getAxioms(frame.getSubject(), pv, mode));
        }
        for (OWLClass domain : frame.getDomains()) {
            OWLAxiom ax = DataFactory.get().getOWLDataPropertyDomainAxiom(frame.getSubject(), domain);
            result.add(ax);
        }
        for (OWLDatatype range : frame.getRanges()) {
            OWLAxiom ax = DataFactory.get().getOWLDataPropertyRangeAxiom(frame.getSubject(), range);
            result.add(ax);
        }
        if (frame.isFunctional()) {
            result.add(DataFactory.get().getOWLFunctionalDataPropertyAxiom(frame.getSubject()));
        }
        return result;
    }
}
