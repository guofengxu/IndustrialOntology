package org.industrial.ontology.kernel.search;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.entity.EntityNodeRenderer;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.EntityType;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EntitySearcher}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.search.EntitySearcherFactory} (generated in the legacy build).
 */
public final class EntitySearcherFactory {

    private final Supplier<ProjectId> projectId;

    private final Supplier<DictionaryManager> dictionaryManager;

    private final Supplier<EntityNodeRenderer> entityNodeRenderer;

    public EntitySearcherFactory(Supplier<ProjectId> projectId,
            Supplier<DictionaryManager> dictionaryManager,
            Supplier<EntityNodeRenderer> entityNodeRenderer) {
        this.projectId = java.util.Objects.requireNonNull(projectId);
        this.dictionaryManager = java.util.Objects.requireNonNull(dictionaryManager);
        this.entityNodeRenderer = java.util.Objects.requireNonNull(entityNodeRenderer);
    }

    public EntitySearcher create(@Nonnull Set<EntityType<?>> entityTypes, @Nonnull String searchString, @Nonnull UserId userId, @Nonnull ImmutableList<DictionaryLanguage> searchLanguages, ImmutableList<EntitySearchFilter> searchFilters) {
        return new EntitySearcher(projectId.get(), dictionaryManager.get(), entityTypes, searchString, userId, searchLanguages, searchFilters, entityNodeRenderer.get());
    }
}
