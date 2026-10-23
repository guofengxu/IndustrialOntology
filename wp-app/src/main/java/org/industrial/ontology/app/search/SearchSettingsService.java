package org.industrial.ontology.app.search;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.util.RegexTimeLimits;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_PROJECT_SETTINGS;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;

/**
 * A project's search filters (docs/01 §5.1, stage S7): the legacy {@code GetSearchSettings} and
 * {@code SetSearchSettings} handlers. Reading needs {@code ViewProject}, replacing {@code EditProjectSettings}, as
 * before.
 * <p>
 * The project's Lucene documents record which filters each entity passes, so replacing the filters rebuilds the
 * project's Lucene index, as the legacy manager did; the rebuild holds the project's write lock so that no change
 * updates the index meanwhile. Unlike the legacy handler, the list replaces the project's filters (the legacy
 * repository never deleted a filter), and a filter id of another project is refused instead of overwriting that
 * project's filter (07 "处理记录（第八轮）").
 */
public class SearchSettingsService {

    public static final String SEARCH_FILTER_OF_ANOTHER_PROJECT = "SEARCH_FILTER_OF_ANOTHER_PROJECT";

    private final ProjectContexts projects;

    private final EntitySearchFilterRepository repository;

    public SearchSettingsService(@Nonnull ProjectContexts projects, @Nonnull EntitySearchFilterRepository repository) {
        this.projects = checkNotNull(projects);
        this.repository = checkNotNull(repository);
    }

    /**
     * The project's search filters.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     */
    @Nonnull
    public List<EntitySearchFilter> getSearchFilters(@Nonnull UserId caller, @Nonnull ProjectId projectId) {
        projects.open(caller, projectId, VIEW_PROJECT);
        return repository.getSearchFilters(projectId);
    }

    /**
     * Makes the filters the project's search filters and rebuilds its Lucene index.
     *
     * @return the project's filters as stored
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the project settings
     * @throws WpException               {@code INVALID_REQUEST} for a filter of another project id, two filters
     *                                   with one id or criteria that cannot be matched (such as a regular expression
     *                                   that does not compile); {@value #SEARCH_FILTER_OF_ANOTHER_PROJECT} (409) for
     *                                   the id of another project's filter; {@value RegexBudget#REGEX_TIMEOUT} (400)
     *                                   when a regular expression takes more than a second on one value while the
     *                                   index is rebuilt, after which the previous filters are restored. Otherwise
     *                                   nothing is changed.
     */
    @Nonnull
    public List<EntitySearchFilter> setSearchFilters(@Nonnull UserId caller,
                                                     @Nonnull ProjectId projectId,
                                                     @Nonnull List<EntitySearchFilter> filters) {
        var context = projects.open(caller, projectId, EDIT_PROJECT_SETTINGS);
        var ids = new HashSet<>();
        for (var filter : filters) {
            if (!filter.getProjectId().equals(projectId)) {
                throw WpException.invalidRequest("Search filter " + filter.getId().getId()
                                                         + " names another project");
            }
            if (!ids.add(filter.getId())) {
                throw WpException.invalidRequest("Two search filters have the id " + filter.getId().getId());
            }
            var owner = repository.findProjectOf(filter.getId());
            if (owner.isPresent() && !owner.get().equals(projectId)) {
                throw new WpException(SEARCH_FILTER_OF_ANOTHER_PROJECT, 409,
                                      "Search filter " + filter.getId().getId() + " is another project's");
            }
            requireMatchable(context, filter);
        }
        var previous = repository.getSearchFilters(projectId);
        repository.replaceSearchFilters(projectId, filters);
        try {
            RegexTimeLimits.withLimitPerMatch(RegexBudget.REBUILD_LIMIT_PER_MATCH, () -> rebuild(context));
        } catch (RegexTimeLimits.Exceeded e) {
            // The filters are evaluated whenever an entity's search document is written, also while changes are
            // applied, without a limit there; a filter that is too slow now must not stay
            repository.replaceSearchFilters(projectId, previous);
            rebuild(context);
            throw RegexBudget.timeout();
        }
        return repository.getSearchFilters(projectId);
    }

    /**
     * Builds the filter's matcher, which compiles its regular expressions, before anything is saved; the legacy
     * handler saved a filter that did not compile, after which every rebuild and every write of the project's search
     * documents failed.
     */
    private static void requireMatchable(ProjectContext context, EntitySearchFilter filter) {
        try {
            context.matcherFactory().getMatcher(filter.getEntityMatchCriteria());
        } catch (PatternSyntaxException e) {
            throw RegexBudget.invalid(e);
        } catch (RuntimeException e) {
            throw WpException.invalidRequest("The criteria of search filter " + filter.getId().getId()
                                                     + " cannot be matched: " + e.getMessage());
        }
    }

    /**
     * Rebuilds the project's search index under its write lock, so that no change writes the index meanwhile.
     */
    private static Void rebuild(ProjectContext context) {
        var lock = context.lock().writeLock();
        lock.lock();
        try {
            context.searchIndex().rebuildIndex();
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not rebuild the search index of project " + context.projectId(), e);
        } finally {
            lock.unlock();
        }
    }
}
