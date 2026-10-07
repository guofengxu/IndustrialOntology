package org.industrial.ontology.kernel.match;



import org.industrial.ontology.kernel.frame.translator.AxiomPropertyValueTranslator;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.frame.State;
import org.industrial.ontology.domain.match.RelationshipPresence;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import javax.annotation.Nonnull;
import java.util.stream.Stream;

import org.industrial.ontology.kernel.api.match.EntityFrameMatcher;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

import org.industrial.ontology.kernel.api.index.PropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityRelationshipMatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-12-02
 */
public class EntityRelationshipMatcher implements EntityFrameMatcher {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final RelationshipPresence relationshipPresence;

    @Nonnull
    private final PropertyValueMatcher propertyValueMatcher;

    @Nonnull
    private final SubClassOfAxiomsBySubClassIndex subClassOfAxiomsBySubClassIndex;

    @Nonnull
    private final PropertyAssertionAxiomsBySubjectIndex propertyAssertionAxiomsBySubjectIndex;

    @Nonnull
    private final AxiomPropertyValueTranslator translator;

    public EntityRelationshipMatcher(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                     @Nonnull RelationshipPresence relationshipPresence,
                                     @Nonnull PropertyValueMatcher propertyValueMatcher,
                                     @Nonnull SubClassOfAxiomsBySubClassIndex subClassOfAxiomsBySubClassIndex,
                                     @Nonnull PropertyAssertionAxiomsBySubjectIndex propertyAssertionAxiomsBySubjectIndex,
                                     @Nonnull AxiomPropertyValueTranslator axiomTranslator) {
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.relationshipPresence = relationshipPresence;
        this.propertyValueMatcher = propertyValueMatcher;
        this.subClassOfAxiomsBySubClassIndex = subClassOfAxiomsBySubClassIndex;
        this.propertyAssertionAxiomsBySubjectIndex = propertyAssertionAxiomsBySubjectIndex;
        this.translator = axiomTranslator;
    }

    @Override
    public boolean matches(@Nonnull OWLEntity entity) {

        Stream<? extends OWLAxiom> axiomStream;
        if(entity.isOWLClass()) {
            axiomStream = projectOntologiesIndex.getOntologyIds()
                    .flatMap(ontId -> {
                        OWLClass cls = entity.asOWLClass();
                        return subClassOfAxiomsBySubClassIndex.getSubClassOfAxiomsForSubClass(cls, ontId);
                    });
        }
        else if(entity.isOWLNamedIndividual()) {
            axiomStream = projectOntologiesIndex.getOntologyIds()
                                                .flatMap(ontId -> {
                                                    OWLNamedIndividual individual = entity.asOWLNamedIndividual();
                                                    return propertyAssertionAxiomsBySubjectIndex.getPropertyAssertions(
                                                            individual, ontId);
                                                });
        }
        else {
            axiomStream = Stream.empty();
        }

        Stream<PlainPropertyValue> propertyValueStream = axiomStream.flatMap(ax -> translator.getPropertyValues(entity, ax, State.ASSERTED).stream());


        if (relationshipPresence == RelationshipPresence.AT_LEAST_ONE) {
            return propertyValueStream.anyMatch(propertyValueMatcher::matches);
        }
        else if(relationshipPresence == RelationshipPresence.AT_MOST_ONE) {
            return propertyValueStream.filter(propertyValueMatcher::matches)
                                        .limit(2)
                                        .count() <= 1;
        }
        else {
            return propertyValueStream.noneMatch(propertyValueMatcher::matches);
        }
    }
}
