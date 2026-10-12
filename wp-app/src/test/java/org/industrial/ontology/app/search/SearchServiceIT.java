package org.industrial.ontology.app.search;

import org.industrial.ontology.domain.match.StringContainsRegexMatchCriteria;
import org.industrial.ontology.domain.match.AnyAnnotationPropertyCriteria;
import org.industrial.ontology.domain.match.EntityAnnotationCriteria;
import org.industrial.ontology.app.frame.AxiomsService;
import org.junit.jupiter.api.Timeout;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectSettingsService;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.lang.LangTag;
import org.industrial.ontology.domain.lang.LangTagFilter;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.match.EntityTypeIsOneOfCriteria;
import org.industrial.ontology.domain.match.HierarchyFilterType;
import org.industrial.ontology.domain.match.SubClassOfCriteria;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.industrial.ontology.domain.search.EntitySearchResult;
import org.industrial.ontology.domain.search.SearchResultMatchPosition;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link SearchService} and {@link SearchSettingsService} (07 5.1-5) on the pizza project: the acceptance of 05
 * P1-06 at the service level.
 */
class SearchServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLEntity MOZZARELLA = PizzaOntology.cls(DATA_FACTORY, "MozzarellaTopping");

    private static final LangTagFilter ANY_LANGUAGE = LangTagFilter.get(ImmutableSet.of());

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectTestFixture fixture;

    private SearchService search;

    private SearchSettingsService searchSettings;

    private ProjectId projectId;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory);
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        fixture = new ProjectTestFixture(context);
        search = context.bean(SearchService.class);
        searchSettings = context.bean(SearchSettingsService.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void mozzShouldFindMozzarellaToppingWhereItMatched() {
        var results = search.search(BOB, projectId, "mozz", Set.of(EntityType.CLASS), ANY_LANGUAGE, List.of(),
                                    PageRequest.requestPageWithSize(1, 10));
        assertThat(results.getPageElements()).isNotEmpty();
        var first = results.getPageElements().get(0);
        assertThat(first.entity().getEntity()).isEqualTo(MOZZARELLA);
        assertThat(first.matches()).anySatisfy(match -> {
            assertThat(match.value()).isEqualTo("Mozzarella Topping");
            assertThat(match.positions()).containsExactly(SearchResultMatchPosition.get(0, 4));
        });
    }

    @Test
    void theLanguageTagFilterShouldChooseTheNamesSearched() {
        context.bean(ProjectSettingsService.class).setProjectLanguages(
                ALICE, projectId, DictionaryLanguage.rdfsLabel("en"),
                DisplayNameSettings.get(ImmutableList.of(DictionaryLanguage.rdfsLabel("en"),
                                                         DictionaryLanguage.rdfsLabel("zh")),
                                        ImmutableList.of()));
        var pizza = PizzaOntology.cls(DATA_FACTORY, "Pizza");

        assertThat(entities(searchIn("比萨", "zh"))).contains(pizza);
        assertThat(entities(searchIn("比萨", "en"))).doesNotContain(pizza);
        assertThat(entities(searchIn("mozz", "zh"))).contains(MOZZARELLA);
    }

    @Test
    void criteriaShouldBeMatchedAPageAtATime() {
        var individuals = EntityTypeIsOneOfCriteria.get(ImmutableSet.of(EntityType.NAMED_INDIVIDUAL));
        var firstPage = search.match(BOB, projectId, individuals, PageRequest.requestPageWithSize(1, 5));
        assertThat(firstPage.getPageElements()).hasSize(5)
                                               .allSatisfy(node -> assertThat(node.getEntity().isOWLNamedIndividual())
                                                       .isTrue());
        assertThat(firstPage.getTotalElements()).isGreaterThan(5);
        var secondPage = search.match(BOB, projectId, individuals, PageRequest.requestPageWithSize(2, 5));
        assertThat(secondPage.getPageNumber()).isEqualTo(2);
        assertThat(secondPage.getPageElements()).doesNotContainAnyElementsOf(firstPage.getPageElements());
        assertThat(firstPage.getPageElements()).extracting(EntityNode::getBrowserText)
                                               .isSortedAccordingTo(String::compareTo);
    }

    @Test
    void theStoredSearchFiltersShouldRestrictTheResults() {
        var cheeses = filter(EntitySearchFilterId.createFilterId(), "Cheeses", "CheeseTopping");
        assertThat(searchSettings.setSearchFilters(ALICE, projectId, List.of(cheeses))).containsExactly(cheeses);

        var unfiltered = entities(search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                                List.of(), PageRequest.requestPageWithSize(1, 100))
                                        .getPageElements());
        var filtered = entities(search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                              List.of(cheeses.getId()), PageRequest.requestPageWithSize(1, 100))
                                      .getPageElements());
        assertThat(unfiltered).contains(MOZZARELLA, PizzaOntology.cls(DATA_FACTORY, "TomatoTopping"));
        assertThat(filtered).contains(MOZZARELLA)
                            .doesNotContain(PizzaOntology.cls(DATA_FACTORY, "TomatoTopping"));
        assertThatThrownBy(() -> search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                               List.of(EntitySearchFilterId.createFilterId()),
                                               PageRequest.requestPageWithSize(1, 100)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    /**
     * Regular expressions of criteria and search filters are matched within limits (S7 security review): without
     * them, ((q+)+)+$ on forty q's would hold the project's read lock, or its write lock while the index is rebuilt.
     */
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void regularExpressionsThatAreSlowOrInvalidShouldBeRefused() {
        context.bean(AxiomsService.class).addAxioms(ALICE, projectId,
                "Ontology(AnnotationAssertion(<http://www.w3.org/2000/01/rdf-schema#comment> <%s> \"%s!\"))"
                        .formatted(PizzaOntology.iri("Margherita"), "q".repeat(40)),
                AxiomsService.Syntax.FUNCTIONAL, null);
        var slow = regexCriteria("((q+)+)+$");
        assertThatThrownBy(() -> search.match(BOB, projectId, slow, PageRequest.requestPageWithSize(1, 10)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo(RegexBudget.REGEX_TIMEOUT);
        assertThatThrownBy(() -> search.match(BOB, projectId, regexCriteria("(unclosed"),
                                              PageRequest.requestPageWithSize(1, 10)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");

        var cheeses = filter(EntitySearchFilterId.createFilterId(), "Cheeses", "CheeseTopping");
        searchSettings.setSearchFilters(ALICE, projectId, List.of(cheeses));
        var invalid = EntitySearchFilter.get(EntitySearchFilterId.createFilterId(), projectId,
                                             LanguageMap.of("en", "Invalid"), regexCriteria("(unclosed"));
        assertThatThrownBy(() -> searchSettings.setSearchFilters(ALICE, projectId, List.of(invalid)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        var slowFilter = EntitySearchFilter.get(EntitySearchFilterId.createFilterId(), projectId,
                                                LanguageMap.of("en", "Slow"), slow);
        assertThatThrownBy(() -> searchSettings.setSearchFilters(ALICE, projectId, List.of(slowFilter)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo(RegexBudget.REGEX_TIMEOUT);
        // The filters from before are back, in the index too
        assertThat(searchSettings.getSearchFilters(BOB, projectId)).containsExactly(cheeses);
        assertThat(entities(search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                          List.of(cheeses.getId()), PageRequest.requestPageWithSize(1, 100))
                                  .getPageElements()))
                .contains(MOZZARELLA)
                .doesNotContain(PizzaOntology.cls(DATA_FACTORY, "TomatoTopping"));
        // Still so once a change has committed the index: the refused rebuild leaves no deletions pending
        context.bean(AxiomsService.class).addAxioms(ALICE, projectId,
                "Ontology(AnnotationAssertion(<http://www.w3.org/2000/01/rdf-schema#comment> <%s> \"Mild\"))"
                        .formatted(PizzaOntology.iri("TomatoTopping")),
                AxiomsService.Syntax.FUNCTIONAL, null);
        assertThat(entities(search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                          List.of(cheeses.getId()), PageRequest.requestPageWithSize(1, 100))
                                  .getPageElements()))
                .contains(MOZZARELLA);
        assertThat(entities(search.search(BOB, projectId, "topping", Set.of(EntityType.CLASS), ANY_LANGUAGE,
                                          List.of(), PageRequest.requestPageWithSize(1, 100))
                                  .getPageElements()))
                .contains(MOZZARELLA, PizzaOntology.cls(DATA_FACTORY, "TomatoTopping"));
    }

    private static EntityAnnotationCriteria regexCriteria(String pattern) {
        return EntityAnnotationCriteria.get(AnyAnnotationPropertyCriteria.get(),
                                            StringContainsRegexMatchCriteria.get(pattern, false));
    }

    @Test
    void theSearchFiltersShouldBeReplacedAndStayInTheirProject() {
        var cheeses = filter(EntitySearchFilterId.createFilterId(), "Cheeses", "CheeseTopping");
        var vegetables = filter(EntitySearchFilterId.createFilterId(), "Vegetables", "VegetableTopping");
        searchSettings.setSearchFilters(ALICE, projectId, List.of(cheeses, vegetables));
        assertThat(searchSettings.getSearchFilters(BOB, projectId)).containsExactlyInAnyOrder(cheeses, vegetables);
        searchSettings.setSearchFilters(ALICE, projectId, List.of(vegetables));
        assertThat(searchSettings.getSearchFilters(BOB, projectId)).containsExactly(vegetables);

        var other = fixture.createProject(ALICE, "Other");
        var stolen = EntitySearchFilter.get(vegetables.getId(), other, vegetables.getLabel(),
                                            vegetables.getEntityMatchCriteria());
        assertThatThrownBy(() -> searchSettings.setSearchFilters(ALICE, other, List.of(stolen)))
                .isInstanceOf(WpException.class)
                .satisfies(e -> {
                    assertThat(((WpException) e).getCode())
                            .isEqualTo(SearchSettingsService.SEARCH_FILTER_OF_ANOTHER_PROJECT);
                    assertThat(((WpException) e).getStatus()).isEqualTo(409);
                });
        assertThatThrownBy(() -> searchSettings.setSearchFilters(ALICE, other, List.of(cheeses)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThat(searchSettings.getSearchFilters(BOB, projectId)).containsExactly(vegetables);
        assertThatThrownBy(() -> searchSettings.setSearchFilters(BOB, projectId, List.of()))
                .isInstanceOf(PermissionDeniedException.class);
        // The kernel's port refuses them too
        var otherProjectsFilters = context.bean(ProjectPorts.class).entitySearchFiltersManager(other);
        assertThatThrownBy(() -> otherProjectsFilters.setSearchFilters(ImmutableList.of(stolen)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> otherProjectsFilters.setSearchFilters(ImmutableList.of(cheeses)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(searchSettings.getSearchFilters(BOB, projectId)).containsExactly(vegetables);
    }

    private List<EntitySearchResult> searchIn(String searchString, String langTag) {
        return search.search(BOB, projectId, searchString, Set.of(EntityType.CLASS),
                             LangTagFilter.get(ImmutableSet.of(LangTag.get(langTag))), List.of(),
                             PageRequest.requestPageWithSize(1, 100))
                     .getPageElements();
    }

    private EntitySearchFilter filter(EntitySearchFilterId id, String label, String parent) {
        return EntitySearchFilter.get(id, projectId, LanguageMap.of("en", label),
                                      SubClassOfCriteria.get(PizzaOntology.cls(DATA_FACTORY, parent),
                                                             HierarchyFilterType.ALL));
    }

    private static List<OWLEntity> entities(List<EntitySearchResult> results) {
        return results.stream().map(result -> result.entity().getEntity()).toList();
    }
}
