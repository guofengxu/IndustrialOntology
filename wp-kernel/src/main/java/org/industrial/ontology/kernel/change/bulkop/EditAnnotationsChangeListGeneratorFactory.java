package org.industrial.ontology.kernel.change.bulkop;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.domain.bulkop.NewAnnotationData;
import org.industrial.ontology.domain.bulkop.Operation;
import javax.annotation.Nonnull;
import java.util.Optional;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EditAnnotationsChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.bulkop.EditAnnotationsChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class EditAnnotationsChangeListGeneratorFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    private final Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionsIndex;

    public EditAnnotationsChangeListGeneratorFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<ProjectOntologiesIndex> projectOntologiesIndex,
            Supplier<AnnotationAssertionAxiomsBySubjectIndex> annotationAssertionsIndex) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.annotationAssertionsIndex = java.util.Objects.requireNonNull(annotationAssertionsIndex);
    }

    public EditAnnotationsChangeListGenerator create(@Nonnull ImmutableSet<OWLEntity> entities, @Nonnull Operation operation, @Nonnull Optional<OWLAnnotationProperty> matchProperty, @Nonnull Optional<String> matchLexicalValue, boolean regEx, @Nonnull Optional<String> matchLangTag, @Nonnull NewAnnotationData newAnnotationData, @Nonnull String commitMessage) {
        return new EditAnnotationsChangeListGenerator(dataFactory.get(), projectOntologiesIndex.get(), annotationAssertionsIndex.get(), entities, operation, matchProperty, matchLexicalValue, regEx, matchLangTag, newAnnotationData, commitMessage);
    }
}
