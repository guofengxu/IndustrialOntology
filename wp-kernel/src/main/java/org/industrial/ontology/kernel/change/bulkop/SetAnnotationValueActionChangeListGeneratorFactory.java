package org.industrial.ontology.kernel.change.bulkop;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInOntologySignatureIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link SetAnnotationValueActionChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.bulkop.SetAnnotationValueActionChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class SetAnnotationValueActionChangeListGeneratorFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    private final Supplier<EntitiesInOntologySignatureIndex> entitiesInSignature;

    private final Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionBySubject;

    public SetAnnotationValueActionChangeListGeneratorFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<ProjectOntologiesIndex> projectOntologiesIndex,
            Supplier<EntitiesInOntologySignatureIndex> entitiesInSignature,
            Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionBySubject) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.entitiesInSignature = java.util.Objects.requireNonNull(entitiesInSignature);
        this.annotationAssertionBySubject = java.util.Objects.requireNonNull(annotationAssertionBySubject);
    }

    public SetAnnotationValueActionChangeListGenerator create(@Nonnull ImmutableSet<OWLEntity> entities, @Nonnull OWLAnnotationProperty property, @Nonnull OWLAnnotationValue value, @Nonnull String commitMessage) {
        return new SetAnnotationValueActionChangeListGenerator(dataFactory.get(), entities, property, value, commitMessage, projectOntologiesIndex.get(), entitiesInSignature.get(), annotationAssertionBySubject.get());
    }
}
