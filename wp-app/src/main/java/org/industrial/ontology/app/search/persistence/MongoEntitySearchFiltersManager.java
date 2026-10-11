package org.industrial.ontology.app.search.persistence;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.kernel.api.repository.ProjectEntitySearchFiltersManager;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The kernel's {@link ProjectEntitySearchFiltersManager} port for one project, over {@code EntitySearchFilters}: the
 * kernel writes these filters into the project's Lucene documents.
 * <p>
 * The legacy {@code ProjectEntitySearchFiltersManagerImpl} also rebuilt the project's search filter index after
 * saving. That needs the loaded project's Lucene index, so it is the search settings service's job (S7); this port
 * only saves.
 */
public class MongoEntitySearchFiltersManager implements ProjectEntitySearchFiltersManager {

    private final ProjectId projectId;

    private final EntitySearchFilterRepository repository;

    public MongoEntitySearchFiltersManager(@Nonnull ProjectId projectId,
                                           @Nonnull EntitySearchFilterRepository repository) {
        this.projectId = checkNotNull(projectId);
        this.repository = checkNotNull(repository);
    }

    @Nonnull
    @Override
    public ImmutableList<EntitySearchFilter> getSearchFilters() {
        return repository.getSearchFilters(projectId);
    }

    @Override
    public void setSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> searchFilters) {
        repository.saveSearchFilters(searchFilters);
    }
}
