package org.industrial.ontology.app.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.tag.persistence.EntityTagsDocument;
import org.industrial.ontology.app.tag.persistence.EntityTagsRepository;
import org.industrial.ontology.app.tag.persistence.MongoTagsManager;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.domain.color.Color;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.match.CompositeRootCriteria;
import org.industrial.ontology.domain.match.HierarchyFilterType;
import org.industrial.ontology.domain.match.MultiMatchType;
import org.industrial.ontology.domain.match.RootCriteria;
import org.industrial.ontology.domain.match.SubClassOfCriteria;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.industrial.ontology.domain.lang.LanguageMap;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.app.project.ProjectServiceIT.ALICE;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_COMMENT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;

/**
 * {@link MongoProjectPorts}: projects that the registry loads get their tags, permission checks and search filters
 * from Mongo and the access manager (S6, 07 2-3).
 */
class MongoProjectPortsIT {

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectTestFixture fixture;

    private ProjectRegistry projectRegistry;

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
        projectRegistry = context.bean(ProjectRegistry.class);
    }

    @Test
    void theRuntimeShouldUseTheMongoPorts() {
        assertThat(context.bean(ProjectPorts.class)).isInstanceOf(MongoProjectPorts.class);
    }

    @Test
    void entitiesShouldHaveTheirAssignedTagsAndTheTagsWhoseCriteriaTheyMatch() {
        var projectId = fixture.createPizzaProject(ALICE, sources);
        var project = projectRegistry.get(projectId);
        var dataFactory = project.dataFactory();
        var margherita = PizzaOntology.cls(dataFactory, "Margherita");
        var pizzas = criteria(SubClassOfCriteria.get(PizzaOntology.cls(dataFactory, "Pizza"), HierarchyFilterType.ALL));
        var vegetarian = tag(projectId, "vegetarian", List.of());
        var pizza = tag(projectId, "pizza", List.of(pizzas));
        var elsewhere = tag(ProjectId.get("00000000-0000-0000-0000-000000000001"), "elsewhere", List.of(pizzas));
        var tags = context.bean(TagRepository.class);
        tags.saveTags(List.of(vegetarian, pizza, elsewhere));
        context.bean(EntityTagsRepository.class)
               .save(EntityTagsDocument.of(projectId, margherita, List.of(vegetarian.getTagId(), pizza.getTagId(),
                                                                          TagId.createTagId())));

        assertThat(project.entityNodeRenderer().render(margherita).getTags()).containsExactlyInAnyOrder(vegetarian,
                                                                                                       pizza);
        // The renderer collects the tags into a set; the port itself also lists each tag once, assigned ones first
        var tagsManager = new MongoTagsManager(projectId, tags, context.bean(EntityTagsRepository.class),
                                               project.matchingEngine());
        assertThat(tagsManager.getTags(margherita)).containsExactly(vegetarian, pizza);
        assertThat(project.entityNodeRenderer().render(PizzaOntology.cls(dataFactory, "Funghi")).getTags())
                .containsExactly(pizza);
        assertThat(project.entityNodeRenderer().render(PizzaOntology.cls(dataFactory, "MozzarellaTopping"))
                          .getTags()).isEmpty();
    }

    @Test
    void creatingEntitiesShouldNeedTheCreatePermissions() {
        var modeller = ProjectKernelFixture.USER;
        var projectId = fixture.createProject(ALICE, "Pizza");
        var project = projectRegistry.get(projectId);
        fixture.grantProjectRoles(modeller, projectId, CAN_COMMENT);

        assertThatThrownBy(() -> ProjectKernelFixture.createClass(project, "Calzone", ImmutableSet.of()))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessage("You do not have permission to create new classes");

        fixture.grantProjectRoles(modeller, projectId, CAN_EDIT);
        assertThat(ProjectKernelFixture.createClass(project, "Calzone", ImmutableSet.of())).isNotNull();
        assertThat(project.revisionManager().getRevisions()).singleElement()
                                                            .satisfies(revision -> assertThat(revision.getUserId())
                                                                    .isEqualTo(modeller));
    }

    @Test
    void theSearchFiltersShouldBeTheProjectsOwn() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        var filter = EntitySearchFilter.get(EntitySearchFilterId.createFilterId(), projectId,
                                            LanguageMap.of("en", "Pizzas"), criteria(
                        SubClassOfCriteria.get(PizzaOntology.cls(projectRegistry.get(projectId).dataFactory(),
                                                                 "Pizza"),
                                               HierarchyFilterType.ALL)));
        context.bean(EntitySearchFilterRepository.class).saveSearchFilters(ImmutableList.of(filter));

        var ports = context.bean(ProjectPorts.class);
        assertThat(ports.entitySearchFiltersManager(projectId).getSearchFilters()).containsExactly(filter);
        assertThat(ports.entitySearchFiltersManager(ProjectId.get("00000000-0000-0000-0000-000000000001"))
                        .getSearchFilters()).isEmpty();
        assertThat(ports.watchManager(projectId).getDirectWatches(PizzaOntology.cls(
                projectRegistry.get(projectId).dataFactory(), "Pizza"), UserId.getUserId("alice"))).isEmpty();
    }

    private static CompositeRootCriteria criteria(RootCriteria criteria) {
        return CompositeRootCriteria.get(ImmutableList.of(criteria), MultiMatchType.ALL);
    }

    private static Tag tag(ProjectId projectId, String label, List<RootCriteria> criteria) {
        return Tag.get(TagId.createTagId(), projectId, label, "", Color.getWhite(), Color.getWhite(), criteria);
    }
}
