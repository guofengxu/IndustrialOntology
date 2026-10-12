package org.industrial.ontology.app.search.persistence;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.kernel.api.repository.ProjectEntitySearchFiltersManager;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The kernel's {@link ProjectEntitySearchFiltersManager} port for one project, over {@code EntitySearchFilters}: the
 * kernel writes these filters into the project's Lucene documents.
 * <p>
 * The legacy {@code ProjectEntitySearchFiltersManagerImpl} also rebuilt the project's search filter index after
 * saving. That needs the loaded project's Lucene index, so it is the search settings service's job (S7); this port
 * only saves. Unlike the legacy manager, it saves only filters of its own project: the repository writes by filter
 * id, so another project's filter id would have replaced that project's filter.
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

    /**
     * @throws IllegalArgumentException for a filter of another project, or whose id another project's filter has
     */
    @Override
    public void setSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> searchFilters) {
        for (var filter : searchFilters) {
            checkArgument(filter.getProjectId().equals(projectId)
                                  && repository.findProjectOf(filter.getId()).map(projectId::equals).orElse(true),
                          "Search filter %s is not of project %s", filter.getId().getId(), projectId);
        }
        repository.saveSearchFilters(searchFilters);
    }
}
