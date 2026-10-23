package org.industrial.ontology.app.search;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DictionaryLanguageFilter;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.lang.LocalNameDictionaryLanguage;
import org.industrial.ontology.domain.lang.OboIdDictionaryLanguage;
import org.industrial.ontology.domain.lang.PrefixedNameDictionaryLanguage;
import org.industrial.ontology.domain.match.RootCriteria;
import org.industrial.ontology.domain.pagination.Page;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.industrial.ontology.domain.search.EntitySearchResult;
import org.industrial.ontology.kernel.api.pagination.PageCollector;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.VIEW_PROJECT;
import static org.industrial.ontology.domain.lang.DictionaryLanguageFilter.EmptyLangTagTreatment.INCLUDE_EMPTY_LANG_TAGS;

/**
 * Finding a project's entities (docs/01 §5.1, stage S7): the legacy {@code PerformEntitySearch}, which searches the
 * names, and {@code GetMatchingEntities}, which evaluates the criteria of the query builder. Both need
 * {@code ViewProject}; the legacy search handler checked nothing.
 */
public class SearchService {

    private final ProjectContexts projects;

    private final EntitySearchFilterRepository searchFilters;

    public SearchService(@Nonnull ProjectContexts projects, @Nonnull EntitySearchFilterRepository searchFilters) {
        this.projects = checkNotNull(projects);
        this.searchFilters = checkNotNull(searchFilters);
    }

    /**
     * The entities of the types whose names match the search string, best matches first, a page at a time; each
     * result says which names matched and where. The names searched are those in the display name languages whose
     * language tags the filter includes (names without a language tag always), local names, prefixed names and OBO
     * ids, as before.
     *
     * @param filterIds the project's search filters that the entities must pass; none for all entities
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@code INVALID_REQUEST} for a filter id that the project does not have
     */
    @Nonnull
    public Page<EntitySearchResult> search(@Nonnull UserId caller,
                                           @Nonnull ProjectId projectId,
                                           @Nonnull String searchString,
                                           @Nonnull Set<EntityType<?>> types,
                                           @Nonnull LangTagFilter langTagFilter,
                                           @Nonnull List<EntitySearchFilterId> filterIds,
                                           @Nonnull PageRequest pageRequest) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> {
            var filters = selectFilters(projectId, filterIds);
            var languageFilter = DictionaryLanguageFilter.get(langTagFilter, INCLUDE_EMPTY_LANG_TAGS);
            var languages = ImmutableList.<DictionaryLanguage>builder();
            context.languageManager().getLanguages().stream().filter(languageFilter::isIncluded)
                   .forEach(languages::add);
            languages.add(OboIdDictionaryLanguage.get());
            languages.add(LocalNameDictionaryLanguage.get());
            languages.add(PrefixedNameDictionaryLanguage.get());
            var searcher = context.components()
                                  .entitySearcherFactory()
                                  .create(types, searchString, caller, languages.build(), filters);
            searcher.setPageRequest(pageRequest);
            searcher.invoke();
            return searcher.getResults();
        });
    }

    /**
     * The entities that match the criteria, by display name, a page at a time. The regular expressions of the criteria
     * are matched within {@link RegexBudget#REQUEST_BUDGET}; the legacy handler had no limit while it held the
     * project.
     *
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not view the project
     * @throws WpException               {@value RegexBudget#REGEX_TIMEOUT} (400) for an expression that takes too
     *                                   long, {@code INVALID_REQUEST} for one that does not compile
     */
    @Nonnull
    public Page<EntityNode> match(@Nonnull UserId caller,
                                  @Nonnull ProjectId projectId,
                                  @Nonnull RootCriteria criteria,
                                  @Nonnull PageRequest pageRequest) {
        return projects.read(caller, projectId, VIEW_PROJECT, context -> RegexBudget.limited(() -> {
            var renderer = context.entityNodeRenderer();
            return context.matchingEngine()
                          .match(criteria)
                          .map(context.rendering()::getRendering)
                          .sorted()
                          .collect(PageCollector.toPage(pageRequest.getPageNumber(), pageRequest.getPageSize()))
                          .map(page -> page.transform(data -> renderer.render(data.getEntity())))
                          .orElse(Page.emptyPage());
        }));
    }

    private ImmutableList<EntitySearchFilter> selectFilters(ProjectId projectId, List<EntitySearchFilterId> filterIds) {
        if (filterIds.isEmpty()) {
            return ImmutableList.of();
        }
        var projectFilters = searchFilters.getSearchFilters(projectId)
                                          .stream()
                                          .collect(Collectors.toMap(EntitySearchFilter::getId, Function.identity()));
        return filterIds.stream()
                        .distinct()
                        .map(id -> {
                            var filter = projectFilters.get(id);
                            if (filter == null) {
                                throw WpException.invalidRequest("The project has no search filter " + id.getId());
                            }
                            return filter;
                        })
                        .collect(ImmutableList.toImmutableList());
    }
}
