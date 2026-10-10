package org.industrial.ontology.kernel.project;

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

import javax.annotation.Nonnull;

/**
 * The application-layer collaborators that {@link ProjectContextFactory} wires into a project's kernel. wp-app
 * implements it over its Mongo repositories and access manager (docs/01 §5); the kernel never reaches them directly.
 * <p>
 * Methods without a project id return application-wide services; the others return the view of one project.
 */
public interface ProjectPorts {

    @Nonnull
    ProjectDetailsRepository projectDetailsRepository();

    @Nonnull
    PrefixDeclarationsStore prefixDeclarationsStore();

    @Nonnull
    ProjectEntityCrudKitSettingsRepository entityCrudKitSettingsRepository();

    @Nonnull
    EntityDiscussionThreadRepository entityDiscussionThreadRepository();

    /**
     * The tags of the project's entities: those assigned to an entity and those whose criteria the entity matches,
     * as the legacy {@code TagsManager} with its {@code CriteriaBasedTagsManager}.
     *
     * @param matchingEngine the project's own matching engine, which evaluates the tag criteria
     */
    @Nonnull
    TagsManager tagsManager(@Nonnull ProjectId projectId, @Nonnull MatchingEngine matchingEngine);

    @Nonnull
    WatchManager watchManager(@Nonnull ProjectId projectId);

    /**
     * Checks the {@code CREATE_*} permissions while {@code ChangeManager} mints fresh entities.
     */
    @Nonnull
    ChangePermissionChecker changePermissionChecker(@Nonnull ProjectId projectId);

    /**
     * The search filters that are written into the project's Lucene documents.
     */
    @Nonnull
    ProjectEntitySearchFiltersManager entitySearchFiltersManager(@Nonnull ProjectId projectId);
}
