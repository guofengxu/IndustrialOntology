package org.industrial.ontology.app.entity;

import org.industrial.ontology.app.search.RegexBudget;
import org.industrial.ontology.app.frame.AxiomsService;
import org.junit.jupiter.api.Timeout;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.bulkop.NewAnnotationData;
import org.industrial.ontology.domain.bulkop.Operation;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.api.index.AnnotationAssertionAxiomsBySubjectIndex;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link BulkEditService} (07 5.1-3) on the pizza project.
 */
class BulkEditServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLClass PIZZA = PizzaOntology.cls(DATA_FACTORY, "Pizza");

    private static final OWLClass FUNGHI = PizzaOntology.cls(DATA_FACTORY, "Funghi");

    private static final OWLClass MARGHERITA = PizzaOntology.cls(DATA_FACTORY, "Margherita");

    private static final OWLClass MOZZARELLA = PizzaOntology.cls(DATA_FACTORY, "MozzarellaTopping");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private BulkEditService bulkEdits;

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
        bulkEdits = context.bean(BulkEditService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void classesShouldBeMovedToTheParent() {
        var outcome = bulkEdits.moveToParent(ALICE, projectId, Set.of(MARGHERITA, FUNGHI), PIZZA, null);
        assertThat(outcome.result()).isTrue();
        assertThat(outcome.revision().orElseThrow().getHighLevelDescription())
                .isEqualTo("Moved classes to " + PIZZA.getIRI());
        var classHierarchy = registry.get(projectId).hierarchies().classHierarchy();
        assertThat(classHierarchy.getParents(MARGHERITA)).containsExactly(PIZZA);
        assertThat(classHierarchy.getParents(FUNGHI)).containsExactly(PIZZA);
        assertThatThrownBy(() -> bulkEdits.moveToParent(ALICE, projectId,
                                                        Set.of(PizzaOntology.objectProperty(DATA_FACTORY,
                                                                                            "hasTopping")),
                                                        PIZZA, null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void theValuesThatTheEntitiesHaveForThePropertyShouldBeReplaced() {
        var tasty = DATA_FACTORY.getOWLLiteral("Tasty", "en");
        var outcome = bulkEdits.setAnnotationValue(ALICE, projectId, Set.of(MARGHERITA, FUNGHI),
                                                   DATA_FACTORY.getRDFSLabel(), tasty, "Tasty pizzas");
        assertThat(outcome.result()).containsExactlyInAnyOrder(MARGHERITA, FUNGHI);
        assertThat(outcome.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Tasty pizzas");
        assertThat(labels(MARGHERITA)).containsExactly(tasty);
        assertThat(labels(FUNGHI)).containsExactly(tasty);
        // As before, entities without a value for the property get none
        var comments = bulkEdits.setAnnotationValue(ALICE, projectId, Set.of(MARGHERITA),
                                                    DATA_FACTORY.getRDFSComment(), tasty, null);
        assertThat(comments.revision()).isEmpty();
        assertThat(comments(MARGHERITA)).isEmpty();
    }

    @Test
    void matchingAnnotationsShouldBeReplacedWithRegularExpressionGroups() {
        var outcome = bulkEdits.editAnnotations(ALICE, projectId, Set.of(MOZZARELLA), Operation.REPLACE,
                                                Optional.of(DATA_FACTORY.getRDFSLabel()),
                                                Optional.of("^(.*) Topping$"), true, Optional.of("en"),
                                                NewAnnotationData.get(Optional.empty(), Optional.of("$1 cheese"),
                                                                      Optional.empty()),
                                                null);
        assertThat(outcome.result()).isTrue();
        assertThat(outcome.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Replaced annotations");
        assertThat(labels(MOZZARELLA)).contains(DATA_FACTORY.getOWLLiteral("Mozzarella cheese", "en"))
                                      .doesNotContain(DATA_FACTORY.getOWLLiteral("Mozzarella Topping", "en"));
    }

    @Test
    void matchingAnnotationsShouldBeDeleted() {
        bulkEdits.editAnnotations(ALICE, projectId, Set.of(MARGHERITA), Operation.DELETE,
                                  Optional.of(DATA_FACTORY.getRDFSLabel()), Optional.empty(), false,
                                  Optional.of("zh"), NewAnnotationData.get(Optional.empty(), Optional.empty(),
                                                                           Optional.empty()),
                                  null);
        assertThat(labels(MARGHERITA)).containsExactly(DATA_FACTORY.getOWLLiteral("Margherita", "en"));
    }

    /**
     * S7 security review: an expression that takes too long, a replacement that names a missing group and a
     * built-in class are refused before anything changes.
     */
    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void slowExpressionsBadReplacementsAndBuiltInClassesShouldChangeNothing() {
        context.bean(AxiomsService.class).addAxioms(ALICE, projectId,
                "Ontology(AnnotationAssertion(<http://www.w3.org/2000/01/rdf-schema#comment> <%s> \"%s!\"))"
                        .formatted(MARGHERITA.getIRI(), "a".repeat(40)),
                AxiomsService.Syntax.FUNCTIONAL, null);
        var head = registry.get(projectId).revisionManager().getCurrentRevision();
        assertThatThrownBy(() -> bulkEdits.editAnnotations(ALICE, projectId, Set.of(MARGHERITA), Operation.REPLACE,
                                                           Optional.of(DATA_FACTORY.getRDFSComment()),
                                                           Optional.of("((a+)+)+$"), true, Optional.empty(),
                                                           newValue("b"), null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo(RegexBudget.REGEX_TIMEOUT);
        assertThatThrownBy(() -> bulkEdits.editAnnotations(ALICE, projectId, Set.of(MARGHERITA), Operation.REPLACE,
                                                           Optional.of(DATA_FACTORY.getRDFSLabel()),
                                                           Optional.of("^(.*)$"), true, Optional.empty(),
                                                           newValue("$9"), null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> bulkEdits.moveToParent(ALICE, projectId, Set.of(DATA_FACTORY.getOWLThing()), PIZZA,
                                                        null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThat(registry.get(projectId).revisionManager().getCurrentRevision()).isEqualTo(head);
    }

    private static NewAnnotationData newValue(String value) {
        return NewAnnotationData.get(Optional.empty(), Optional.of(value), Optional.empty());
    }

    @Test
    void badRegularExpressionsAndViewersShouldBeRefused() {
        assertThatThrownBy(() -> bulkEdits.editAnnotations(ALICE, projectId, Set.of(MARGHERITA), Operation.DELETE,
                                                           Optional.empty(), Optional.of("(unclosed"), true,
                                                           Optional.empty(),
                                                           NewAnnotationData.get(Optional.empty(), Optional.empty(),
                                                                                 Optional.empty()),
                                                           null))
                .isInstanceOf(WpException.class)
                // Refused before the changes are generated, not as a group that the replacement lacks
                .hasMessageContaining("not a regular expression")
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> bulkEdits.setAnnotationValue(BOB, projectId, Set.of(MARGHERITA),
                                                              DATA_FACTORY.getRDFSComment(),
                                                              DATA_FACTORY.getOWLLiteral("No"), null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    private List<OWLAnnotationValue> comments(OWLEntity entity) {
        return annotations(entity, DATA_FACTORY.getRDFSComment().getIRI().toString());
    }

    private List<OWLAnnotationValue> labels(OWLEntity entity) {
        return annotations(entity, DATA_FACTORY.getRDFSLabel().getIRI().toString());
    }

    private List<OWLAnnotationValue> annotations(OWLEntity entity, String property) {
        var context = registry.get(projectId);
        return context.indexes()
                      .get(AnnotationAssertionAxiomsBySubjectIndex.class)
                      .getAxiomsForSubject(entity.getIRI(), context.defaultOntologyIdManager().getDefaultOntologyId())
                      .filter(axiom -> axiom.getProperty().getIRI().toString().equals(property))
                      .map(axiom -> axiom.getValue())
                      .toList();
    }
}
