package org.industrial.ontology.kernel.index;



import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import javax.annotation.Nonnull;
import java.util.Collection;

import java.util.List;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.DependentIndex;

import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.DeprecatedEntitiesByEntityIndexImpl}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-15
 */
public class DeprecatedEntitiesByEntityIndex implements org.industrial.ontology.kernel.api.index.DeprecatedEntitiesByEntityIndex, DependentIndex {

    @Nonnull
    private final ProjectOntologiesIndex projectOntologiesIndex;

    @Nonnull
    private final AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex;

    public DeprecatedEntitiesByEntityIndex(@Nonnull ProjectOntologiesIndex projectOntologiesIndex,
                                               @Nonnull AnnotationAssertionAxiomsBySubjectIndex annotationAssertionsIndex) {
        this.projectOntologiesIndex = checkNotNull(projectOntologiesIndex);
        this.annotationAssertionsIndex = checkNotNull(annotationAssertionsIndex);
    }


    @Nonnull
    @Override
    public Collection<Index> getDependencies() {
        return List.of(projectOntologiesIndex, annotationAssertionsIndex);
    }

    @Override
    public boolean isDeprecated(@Nonnull OWLEntity entity) {
        return projectOntologiesIndex
                .getOntologyIds()
                .flatMap(ontId -> annotationAssertionsIndex.getAxiomsForSubject(entity.getIRI(), ontId))
                .anyMatch(OWLAnnotationAssertionAxiom::isDeprecatedIRIAssertion);
    }
}
