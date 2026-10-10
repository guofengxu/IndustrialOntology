package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessChangePermissionChecker;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.search.persistence.MongoEntitySearchFiltersManager;
import org.industrial.ontology.app.tag.persistence.EntityTagsRepository;
import org.industrial.ontology.app.tag.persistence.MongoTagsManager;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.app.watch.persistence.MongoWatchManager;
import org.industrial.ontology.app.watch.persistence.WatchRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.api.match.MatchingEngine;
import org.industrial.ontology.kernel.api.port.ChangePermissionChecker;
import org.industrial.ontology.kernel.api.port.EntityDiscussionThreadRepository;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettingsRepository;
import org.industrial.ontology.kernel.api.port.TagsManager;
import org.industrial.ontology.kernel.api.port.WatchManager;
import org.industrial.ontology.kernel.api.repository.ProjectEntitySearchFiltersManager;
import org.industrial.ontology.kernel.project.ProjectPorts;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * wp-app's {@link ProjectPorts} (docs/01 §2, §5): the Mongo repositories of stage S4 and the access manager of S5,
 * wired into each project's kernel when {@code ProjectRegistry} loads the project. The per-project ports are created
 * once per load and live as long as the project's context.
 */
public class MongoProjectPorts implements ProjectPorts {

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final MongoPrefixDeclarationsStore prefixDeclarationsStore;

    private final MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository;

    private final DiscussionThreadRepository discussionThreadRepository;

    private final TagRepository tagRepository;

    private final EntityTagsRepository entityTagsRepository;

    private final WatchRepository watchRepository;

    private final EntitySearchFilterRepository entitySearchFilterRepository;

    private final AccessManager accessManager;

    public MongoProjectPorts(@Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                             @Nonnull MongoPrefixDeclarationsStore prefixDeclarationsStore,
                             @Nonnull MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository,
                             @Nonnull DiscussionThreadRepository discussionThreadRepository,
                             @Nonnull TagRepository tagRepository,
                             @Nonnull EntityTagsRepository entityTagsRepository,
                             @Nonnull WatchRepository watchRepository,
                             @Nonnull EntitySearchFilterRepository entitySearchFilterRepository,
                             @Nonnull AccessManager accessManager) {
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.prefixDeclarationsStore = checkNotNull(prefixDeclarationsStore);
        this.entityCrudKitSettingsRepository = checkNotNull(entityCrudKitSettingsRepository);
        this.discussionThreadRepository = checkNotNull(discussionThreadRepository);
        this.tagRepository = checkNotNull(tagRepository);
        this.entityTagsRepository = checkNotNull(entityTagsRepository);
        this.watchRepository = checkNotNull(watchRepository);
        this.entitySearchFilterRepository = checkNotNull(entitySearchFilterRepository);
        this.accessManager = checkNotNull(accessManager);
    }

    @Nonnull
    @Override
    public ProjectDetailsRepository projectDetailsRepository() {
        return projectDetailsRepository;
    }

    @Nonnull
    @Override
    public PrefixDeclarationsStore prefixDeclarationsStore() {
        return prefixDeclarationsStore;
    }

    @Nonnull
    @Override
    public ProjectEntityCrudKitSettingsRepository entityCrudKitSettingsRepository() {
        return entityCrudKitSettingsRepository;
    }

    @Nonnull
    @Override
    public EntityDiscussionThreadRepository entityDiscussionThreadRepository() {
        return discussionThreadRepository;
    }

    @Nonnull
    @Override
    public TagsManager tagsManager(@Nonnull ProjectId projectId, @Nonnull MatchingEngine matchingEngine) {
        return new MongoTagsManager(projectId, tagRepository, entityTagsRepository, matchingEngine);
    }

    @Nonnull
    @Override
    public WatchManager watchManager(@Nonnull ProjectId projectId) {
        return new MongoWatchManager(projectId, watchRepository);
    }

    @Nonnull
    @Override
    public ChangePermissionChecker changePermissionChecker(@Nonnull ProjectId projectId) {
        return new AccessChangePermissionChecker(accessManager, projectId);
    }

    @Nonnull
    @Override
    public ProjectEntitySearchFiltersManager entitySearchFiltersManager(@Nonnull ProjectId projectId) {
        return new MongoEntitySearchFiltersManager(projectId, entitySearchFilterRepository);
    }
}
