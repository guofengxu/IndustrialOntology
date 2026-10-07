package org.industrial.ontology.kernel.match;

import org.industrial.ontology.kernel.frame.translator.AxiomPropertyValueTranslator;
import org.industrial.ontology.domain.match.RelationshipPresence;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.PropertyAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EntityRelationshipMatcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.match.EntityRelationshipMatcherFactory} (generated in the legacy build).
 */
public final class EntityRelationshipMatcherFactory {

    private final Supplier<ProjectOntologiesIndex> projectOntologiesIndex;

    private final Supplier<SubClassOfAxiomsBySubClassIndex> subClassOfAxiomsBySubClassIndex;

    private final Supplier<PropertyAssertionAxiomsBySubjectIndex> propertyAssertionAxiomsBySubjectIndex;

    private final Supplier<AxiomPropertyValueTranslator> axiomTranslator;

    public EntityRelationshipMatcherFactory(Supplier<ProjectOntologiesIndex> projectOntologiesIndex,
            Supplier<SubClassOfAxiomsBySubClassIndex> subClassOfAxiomsBySubClassIndex,
            Supplier<PropertyAssertionAxiomsBySubjectIndex> propertyAssertionAxiomsBySubjectIndex,
            Supplier<AxiomPropertyValueTranslator> axiomTranslator) {
        this.projectOntologiesIndex = java.util.Objects.requireNonNull(projectOntologiesIndex);
        this.subClassOfAxiomsBySubClassIndex = java.util.Objects.requireNonNull(subClassOfAxiomsBySubClassIndex);
        this.propertyAssertionAxiomsBySubjectIndex = java.util.Objects.requireNonNull(propertyAssertionAxiomsBySubjectIndex);
        this.axiomTranslator = java.util.Objects.requireNonNull(axiomTranslator);
    }

    public EntityRelationshipMatcher create(@Nonnull RelationshipPresence relationshipPresence, @Nonnull PropertyValueMatcher propertyValueMatcher) {
        return new EntityRelationshipMatcher(projectOntologiesIndex.get(), relationshipPresence, propertyValueMatcher, subClassOfAxiomsBySubClassIndex.get(), propertyAssertionAxiomsBySubjectIndex.get(), axiomTranslator.get());
    }
}
