package org.industrial.ontology.kernel.api.entity;



import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsByValueIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import javax.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

import java.util.stream.Stream;
import static com.google.common.base.Preconditions.checkNotNull;
import org.semanticweb.owlapi.model.IRI;

import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.entity.SubjectClosureResolver}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-13
 *
 * Given an entity, returns other entities that could be the subject of the supplied entity.  This is
 * based on annotation assertions.  For example, given AnnotationAssertion(:p :A :C), the IRI :C would
 * be resolved to {:A :C}.
 */
public class SubjectClosureResolver {

    @Nonnull
    private final AnnotationAssertionAxiomsByValueIndex annotationAssertionAxiomsByValueIndex;

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureIndex;

    public SubjectClosureResolver(@Nonnull AnnotationAssertionAxiomsByValueIndex annotationAssertionAxiomsByValueIndex,
                                  @Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                  @Nonnull EntitiesInProjectSignatureByIriIndex entitiesInProjectSignatureIndex) {
        this.annotationAssertionAxiomsByValueIndex = checkNotNull(annotationAssertionAxiomsByValueIndex);
        this.projectOntologiesIndex = projectOntologiesIndex;
        this.entitiesInProjectSignatureIndex = entitiesInProjectSignatureIndex;
    }

    @Nonnull
    public Stream<OWLEntity> resolve(@Nonnull OWLEntity entity) {
        var resultBuilder = ImmutableSet.<OWLEntity>builder();
        resultBuilder.add(entity);
        var entityIri = entity.getIRI();
        getIncomingEntities(entityIri, resultBuilder, new HashSet<>());
        return resultBuilder.build().stream();
    }

    private void getIncomingEntities(@Nonnull OWLAnnotationValue targetValue,
                                     @Nonnull ImmutableSet.Builder<OWLEntity> resultBuilder,
                                     @Nonnull Set<OWLAnnotationValue> processed) {
        if(!processed.add(targetValue)) {
            return;
        }
        var subjects = getSubjectsForValue(targetValue);
        subjects.forEach(subject -> {
            if(subject instanceof IRI) {
                var subjectIri = (IRI) subject;
                entitiesInProjectSignatureIndex.getEntitiesInSignature(subjectIri)
                                               .forEach(resultBuilder::add);
            }
            var subjectAsValue = (OWLAnnotationValue) subject;
            getIncomingEntities(subjectAsValue, resultBuilder, processed);
        });
    }

    @Nonnull
    private Stream<OWLAnnotationSubject> getSubjectsForValue(@Nonnull OWLAnnotationValue targetValue) {
        return projectOntologiesIndex.getOntologyIds()
                              .flatMap(ontId -> annotationAssertionAxiomsByValueIndex.getAxiomsByValue(targetValue, ontId))
                .map(OWLAnnotationAssertionAxiom::getSubject);
    }
}
