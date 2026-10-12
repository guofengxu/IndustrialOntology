package org.industrial.ontology.app.hierarchy;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.AxiomsService;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.EntityNode;
import org.industrial.ontology.domain.hierarchy.DropType;
import org.industrial.ontology.domain.hierarchy.GraphNode;
import org.industrial.ontology.domain.hierarchy.HierarchyId;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.hierarchy.HierarchyId.CLASS_HIERARCHY;

/**
 * {@link HierarchyService} (07 5.1-3) on the pizza project.
 */
class HierarchyServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLClass PIZZA = PizzaOntology.cls(DATA_FACTORY, "Pizza");

    private static final OWLClass NAMED_PIZZA = PizzaOntology.cls(DATA_FACTORY, "NamedPizza");

    private static final OWLClass FUNGHI = PizzaOntology.cls(DATA_FACTORY, "Funghi");

    private static final OWLClass MARGHERITA = PizzaOntology.cls(DATA_FACTORY, "Margherita");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private HierarchyService hierarchies;

    private ProjectRegistry registry;

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
        var fixture = new ProjectTestFixture(context);
        hierarchies = context.bean(HierarchyService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void theRootsShouldBeTheTopEntities() {
        assertThat(entities(hierarchies.getRoots(BOB, projectId, CLASS_HIERARCHY)))
                .containsExactly(DATA_FACTORY.getOWLThing());
        assertThat(entities(hierarchies.getRoots(BOB, projectId, HierarchyId.OBJECT_PROPERTY_HIERARCHY)))
                .containsExactly(DATA_FACTORY.getOWLTopObjectProperty());
        assertThatThrownBy(() -> hierarchies.getRoots(BOB, projectId, HierarchyId.get("Datatype")))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void theChildrenShouldBeSortedByDisplayNameAPageAtATime() {
        var children = hierarchies.getChildren(BOB, projectId, CLASS_HIERARCHY, NAMED_PIZZA,
                                               PageRequest.requestPageWithSize(1, 100));
        assertThat(children.parent().userObject().getEntity()).isEqualTo(NAMED_PIZZA);
        assertThat(children.parent().sink()).isFalse();
        var names = children.children().getPageElements().stream()
                            .map(node -> node.userObject().getBrowserText()).toList();
        assertThat(names).contains("Margherita", "Funghi")
                         .isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
        assertThat(children.children().getPageElements()).allSatisfy(node -> assertThat(node.sink()).isTrue());

        var secondPage = hierarchies.getChildren(BOB, projectId, CLASS_HIERARCHY, NAMED_PIZZA,
                                                 PageRequest.requestPageWithSize(2, 3));
        assertThat(secondPage.children().getPageElements())
                .containsExactlyElementsOf(children.children().getPageElements().subList(3, 6));
    }

    @Test
    void deprecatedEntitiesDirectlyBelowTheTopShouldNotBeChildren() {
        var obsolete = PizzaOntology.cls(DATA_FACTORY, "Obsolete");
        context.bean(AxiomsService.class).addAxioms(ALICE, projectId, """
                Ontology(
                Declaration(Class(<%1$s>))
                AnnotationAssertion(<http://www.w3.org/2002/07/owl#deprecated> <%1$s> "true"^^<http://www.w3.org/2001/XMLSchema#boolean>)
                )
                """.formatted(obsolete.getIRI()), AxiomsService.Syntax.FUNCTIONAL, null);
        var thing = DATA_FACTORY.getOWLThing();
        assertThat(registry.get(projectId).hierarchies().classHierarchy().getChildren(thing)).contains(obsolete);

        var children = hierarchies.getChildren(BOB, projectId, CLASS_HIERARCHY, thing,
                                               PageRequest.requestPageWithSize(1, 100));
        assertThat(children.children().getPageElements()).isNotEmpty()
                                                         .extracting(node -> node.userObject().getEntity())
                                                         .doesNotContain(obsolete);
    }

    @Test
    void siblingsAndPathsToTheRootShouldBeFound() {
        var siblings = hierarchies.getSiblings(BOB, projectId, CLASS_HIERARCHY, MARGHERITA,
                                               PageRequest.requestPageWithSize(1, 100));
        assertThat(siblings.getPageElements()).extracting(node -> node.userObject().getEntity())
                                              .contains(FUNGHI)
                                              .doesNotContain(MARGHERITA);

        var paths = hierarchies.getPathsToRoot(BOB, projectId, CLASS_HIERARCHY, MARGHERITA);
        assertThat(paths).isNotEmpty().allSatisfy(path -> {
            assertThat(path.getFirst().orElseThrow().userObject().getEntity()).isEqualTo(DATA_FACTORY.getOWLThing());
            assertThat(path.getLast().orElseThrow().userObject().getEntity()).isEqualTo(MARGHERITA);
        });
        assertThat(paths).anySatisfy(path -> assertThat(path.asList()).extracting(node -> node.userObject()
                                                                                              .getEntity())
                                                                      .contains(PIZZA, NAMED_PIZZA));
    }

    @Test
    void builtInEntitiesShouldNotBeMoved() {
        assertThatThrownBy(() -> hierarchies.move(ALICE, projectId, CLASS_HIERARCHY, DATA_FACTORY.getOWLThing(), null,
                                                  PIZZA, DropType.MOVE, null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void anEntityShouldBeMovedOrGivenAnotherParent() {
        var classHierarchy = registry.get(projectId).hierarchies().classHierarchy();
        var moved = hierarchies.move(ALICE, projectId, CLASS_HIERARCHY, FUNGHI, NAMED_PIZZA, PIZZA, DropType.MOVE,
                                     null);
        assertThat(moved.result()).isTrue();
        assertThat(moved.revision().orElseThrow().getHighLevelDescription())
                .isEqualTo("Moved class Funghi from Named Pizza to Pizza");
        assertThat(classHierarchy.getParents(FUNGHI)).containsExactly(PIZZA);

        hierarchies.move(ALICE, projectId, CLASS_HIERARCHY, FUNGHI, PIZZA, NAMED_PIZZA, DropType.COPY,
                         "Back again");
        assertThat(classHierarchy.getParents(FUNGHI)).containsExactlyInAnyOrder(PIZZA, NAMED_PIZZA);

        assertThatThrownBy(() -> hierarchies.move(ALICE, projectId, CLASS_HIERARCHY,
                                                  PizzaOntology.objectProperty(DATA_FACTORY, "hasTopping"),
                                                  null, null, DropType.MOVE, null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> hierarchies.move(BOB, projectId, CLASS_HIERARCHY, FUNGHI, PIZZA, NAMED_PIZZA,
                                                  DropType.MOVE, null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    private static List<OWLEntity> entities(List<GraphNode<EntityNode>> nodes) {
        return nodes.stream().map(node -> node.userObject().getEntity()).toList();
    }
}
