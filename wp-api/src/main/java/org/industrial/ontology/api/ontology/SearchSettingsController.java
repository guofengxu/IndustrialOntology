package org.industrial.ontology.api.ontology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.search.SearchSettingsService;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.EntityMatchCriteria;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.api.ontology.OntologyParams.required;

/**
 * A project's search filters (docs/02 §3 {@code search-settings}): named criteria that searches can be limited to.
 * The service checks the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/search-settings")
@Tag(name = "Search settings", description = "The project's search filters")
public class SearchSettingsController {

    private final SearchSettingsService searchSettingsService;

    public SearchSettingsController(SearchSettingsService searchSettingsService) {
        this.searchSettingsService = checkNotNull(searchSettingsService);
    }

    @Operation(summary = "The project's search filters (permission ViewProject)")
    @GetMapping
    public List<SearchFilterDto> searchFilters(@Caller UserId caller, @PathVariable String projectId) {
        return searchSettingsService.getSearchFilters(caller, ProjectIds.parse(projectId))
                                    .stream()
                                    .map(SearchFilterDto::of)
                                    .toList();
    }

    @Operation(summary = "Replaces the project's search filters (permission EditProjectSettings)",
               description = "A filter without an id gets one. The project's search index is rebuilt.")
    @PutMapping
    public List<SearchFilterDto> setSearchFilters(@Caller UserId caller,
                                                  @PathVariable String projectId,
                                                  @RequestBody List<SearchFilterDto> filters) {
        var project = ProjectIds.parse(projectId);
        var searchFilters = filters.stream()
                                   .map(filter -> {
                                       required(filter, "A search filter");
                                       return EntitySearchFilter.get(
                                               filter.id() == null ? EntitySearchFilterId.createFilterId()
                                                                   : filterId(filter.id()),
                                               project,
                                               Objects.requireNonNullElse(filter.label(), LanguageMap.empty()),
                                               required(filter.criteria(), "criteria"));
                                   })
                                   .toList();
        return searchSettingsService.setSearchFilters(caller, project, searchFilters)
                                    .stream()
                                    .map(SearchFilterDto::of)
                                    .toList();
    }

    private static EntitySearchFilterId filterId(String id) {
        try {
            return EntitySearchFilterId.get(id);
        } catch (IllegalArgumentException e) {
            throw WpException.invalidRequest("A search filter id is a UUID");
        }
    }

    /**
     * A search filter: its label in each language, and the criteria that the entities it lets through match.
     */
    public record SearchFilterDto(String id, LanguageMap label, EntityMatchCriteria criteria) {

        static SearchFilterDto of(EntitySearchFilter filter) {
            return new SearchFilterDto(filter.getId().getId(), filter.getLabel(), filter.getEntityMatchCriteria());
        }
    }
}
