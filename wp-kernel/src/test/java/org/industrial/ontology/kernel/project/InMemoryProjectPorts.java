package org.industrial.ontology.kernel.project;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.watches.Watch;
import org.industrial.ontology.kernel.api.port.ChangePermissionChecker;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettings;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettingsRepository;
import org.industrial.ontology.kernel.api.port.TagsManager;
import org.industrial.ontology.kernel.api.port.WatchManager;
import org.industrial.ontology.kernel.api.repository.ProjectEntitySearchFiltersManager;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ProjectPorts} for kernel tests: a freshly installed application with no project details, tags, watches or
 * discussions, where every change is permitted. Entity CRUD kit settings and search filters are kept in memory so
 * that the kernel can store its defaults.
 */
public class InMemoryProjectPorts implements ProjectPorts {

    private final Map<ProjectId, ProjectEntityCrudKitSettings> crudKitSettings = new ConcurrentHashMap<>();

    private final Map<ProjectId, ImmutableList<EntitySearchFilter>> searchFilters = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public ProjectDetailsRepository projectDetailsRepository() {
        return new ProjectDetailsRepository() {
            @Override
            public Optional<ProjectDetails> findOne(@Nonnull ProjectId projectId) {
                return Optional.empty();
            }

            @Override
            public ImmutableList<DictionaryLanguage> getDisplayNameLanguages(@Nonnull ProjectId projectId) {
                return ImmutableList.of();
            }
        };
    }

    @Nonnull
    @Override
    public PrefixDeclarationsStore prefixDeclarationsStore() {
        return PrefixDeclarations::get;
    }

    @Nonnull
    @Override
    public ProjectEntityCrudKitSettingsRepository entityCrudKitSettingsRepository() {
        return new ProjectEntityCrudKitSettingsRepository() {
            @Override
            public Optional<ProjectEntityCrudKitSettings> findOne(@Nonnull ProjectId projectId) {
                return Optional.ofNullable(crudKitSettings.get(projectId));
            }

            @Override
            public void save(@Nonnull ProjectEntityCrudKitSettings settings) {
                crudKitSettings.put(settings.getProjectId(), settings);
            }
        };
    }

    @Nonnull
    @Override
    public EntityDiscussionThreadRepository entityDiscussionThreadRepository() {
        return new EntityDiscussionThreadRepository() {
            @Override
            public int getOpenCommentsCount(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
                return 0;
            }

            @Override
            public void replaceEntity(@Nonnull ProjectId projectId,
                                      @Nonnull OWLEntity entity,
                                      @Nonnull OWLEntity withEntity) {
            }
        };
    }

    @Nonnull
    @Override
    public TagsManager tagsManager(@Nonnull ProjectId projectId) {
        return entity -> Set.of();
    }

    @Nonnull
    @Override
    public WatchManager watchManager(@Nonnull ProjectId projectId) {
        return new WatchManager() {
            @Override
            public Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity) {
                return Set.of();
            }

            @Override
            public Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity, @Nonnull UserId userId) {
                return Set.of();
            }
        };
    }

    @Nonnull
    @Override
    public ChangePermissionChecker changePermissionChecker(@Nonnull ProjectId projectId) {
        return ChangePermissionChecker.ALLOW_ALL;
    }

    @Nonnull
    @Override
    public ProjectEntitySearchFiltersManager entitySearchFiltersManager(@Nonnull ProjectId projectId) {
        return new ProjectEntitySearchFiltersManager() {
            @Nonnull
            @Override
            public ImmutableList<EntitySearchFilter> getSearchFilters() {
                return searchFilters.getOrDefault(projectId, ImmutableList.of());
            }

            @Override
            public void setSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> filters) {
                searchFilters.put(projectId, filters);
            }
        };
    }
}
