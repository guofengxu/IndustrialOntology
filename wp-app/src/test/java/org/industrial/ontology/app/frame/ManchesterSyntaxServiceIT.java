package org.industrial.ontology.app.frame;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.CheckStatus;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.Choice;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.FreshEntity;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.ManchesterSyntaxFrameException;
import org.industrial.ontology.app.frame.ManchesterSyntaxService.Position;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.ClassFrameTranslationOptions;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link ManchesterSyntaxService} (07 5.1-1) on the pizza project, whose display names are the English labels.
 */
class ManchesterSyntaxServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLClass MARGHERITA = PizzaOntology.cls(DATA_FACTORY, "Margherita");

    private static final String BARE_FRAME = "Class: Margherita";

    private static final String WITH_PIZZA_PARENT = "Class: Margherita\n    SubClassOf: Pizza";

    private static final String WITH_UNKNOWN_PARENT = "Class: Margherita\n    SubClassOf: Calzone";

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ManchesterSyntaxService manchesterSyntax;

    private ProjectRegistry registry;

    private ProjectTestFixture fixture;

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
        manchesterSyntax = context.bean(ManchesterSyntaxService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void theFrameShouldBeManchesterSyntaxWithDisplayNames() {
        var frame = manchesterSyntax.getFrame(BOB, projectId, MARGHERITA);
        assertThat(frame.subject().getEntity()).isEqualTo(MARGHERITA);
        assertThat(frame.text()).startsWith("Class: Margherita")
                                .contains("SubClassOf: ")
                                .contains("'Named Pizza'");
    }

    @Test
    void checkingShouldTellWhetherTheEditChangesAnythingOrWhereItFails() {
        assertThat(manchesterSyntax.check(BOB, projectId, MARGHERITA, BARE_FRAME, WITH_PIZZA_PARENT, Set.of())
                                   .status()).isEqualTo(CheckStatus.CHANGED);
        assertThat(manchesterSyntax.check(BOB, projectId, MARGHERITA, BARE_FRAME, BARE_FRAME, Set.of()).status())
                .isEqualTo(CheckStatus.UNCHANGED);
        var failed = manchesterSyntax.check(BOB, projectId, MARGHERITA, BARE_FRAME, WITH_UNKNOWN_PARENT, Set.of());
        assertThat(failed.status()).isEqualTo(CheckStatus.ERROR);
        assertThat(failed.error().getLine()).isEqualTo(2);
        assertThat(failed.error().getCol()).isEqualTo(17);
        assertThat(failed.error().getToken()).isEqualTo("Calzone");
        assertThat(failed.error().getExpectedEntityTypes()).contains(EntityType.CLASS);
        // OWL API counts the columns of the first line from 1, of the others from 0; both start at 1 here
        var firstLine = manchesterSyntax.check(BOB, projectId, MARGHERITA, BARE_FRAME, "Class: Calzone", Set.of());
        assertThat(firstLine.error().getLine()).isEqualTo(1);
        assertThat(firstLine.error().getCol()).isEqualTo(8);
    }

    @Test
    void settingTheFrameShouldApplyTheEditAndReturnTheFrameAfterIt() {
        var head = registry.get(projectId).revisionManager().getCurrentRevision();
        var outcome = manchesterSyntax.setFrame(ALICE, projectId, MARGHERITA, BARE_FRAME, WITH_PIZZA_PARENT,
                                                "Margherita is a pizza", Set.of());
        var revision = outcome.revision().orElseThrow();
        assertThat(revision.getRevisionNumber().getValue()).isEqualTo(head.getValue() + 1);
        // As before, the message follows the generated description
        assertThat(revision.getHighLevelDescription())
                .isEqualTo("Edited description of Margherita.\nMargherita is a pizza");
        assertThat(outcome.result()).isEqualTo(manchesterSyntax.getFrame(ALICE, projectId, MARGHERITA).text());
        assertThat(parentsOfMargherita()).contains(PizzaOntology.cls(DATA_FACTORY, "Pizza"));
    }

    @Test
    void aFrameThatDoesNotParseShouldBeRefusedWithWhereItFailed() {
        var head = registry.get(projectId).revisionManager().getCurrentRevision();
        assertThatThrownBy(() -> manchesterSyntax.setFrame(ALICE, projectId, MARGHERITA, BARE_FRAME,
                                                           WITH_UNKNOWN_PARENT, null, Set.of()))
                .isInstanceOfSatisfying(ManchesterSyntaxFrameException.class, e -> {
                    assertThat(e.getCode()).isEqualTo(ManchesterSyntaxService.MANCHESTER_SYNTAX_ERROR);
                    assertThat(e.getStatus()).isEqualTo(400);
                    assertThat(e.getProperties()).containsEntry("line", 2)
                                                 .containsEntry("column", 17)
                                                 .containsEntry("token", "Calzone");
                });
        assertThat(registry.get(projectId).revisionManager().getCurrentRevision()).isEqualTo(head);
    }

    @Test
    void freshEntitiesShouldBeCreatedWhenTheFrameIsSet() {
        var calzone = Set.of(new FreshEntity("Calzone", EntityType.CLASS));
        assertThat(manchesterSyntax.check(ALICE, projectId, MARGHERITA, BARE_FRAME, WITH_UNKNOWN_PARENT, calzone)
                                   .status()).isEqualTo(CheckStatus.CHANGED);

        manchesterSyntax.setFrame(ALICE, projectId, MARGHERITA, BARE_FRAME, WITH_UNKNOWN_PARENT, null, calzone);

        var created = registry.get(projectId).dictionary().getEntities("Calzone").toList();
        assertThat(created).hasSize(1).allSatisfy(entity -> assertThat(entity.isOWLClass()).isTrue());
        assertThat(parentsOfMargherita()).contains(created.get(0).asOWLClass());
    }

    @Test
    void completionsShouldProposeTheEntitiesAndKeywordsExpectedAtTheCursor() {
        var text = "Class: Margherita\n    SubClassOf: Mozz";
        var completions = manchesterSyntax.getCompletions(BOB, projectId, text, text.length(), Set.of(), 20);
        assertThat(completions.choices()).contains(new Choice("'Mozzarella Topping'", "Mozzarella Topping", ""));
        assertThat(completions.from()).isEqualTo(new Position(2, 17));
        assertThat(completions.to()).isEqualTo(new Position(2, 21));

        var keyword = "Class: Margherita\n    SubCl";
        assertThat(manchesterSyntax.getCompletions(BOB, projectId, keyword, keyword.length(), Set.of(), 20)
                                   .choices())
                .extracting(Choice::text)
                .contains("SubClassOf:");

        assertThatThrownBy(() -> manchesterSyntax.getCompletions(BOB, projectId, keyword, keyword.length() + 1,
                                                                 Set.of(), 20))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void settingNeedsEditOntologyAndTheRestViewProject() {
        assertThatThrownBy(() -> manchesterSyntax.setFrame(BOB, projectId, MARGHERITA, BARE_FRAME,
                                                           WITH_PIZZA_PARENT, null, Set.of()))
                .isInstanceOf(PermissionDeniedException.class);
        var stranger = UserId.getUserId("carol");
        assertThatThrownBy(() -> manchesterSyntax.getFrame(stranger, projectId, MARGHERITA))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> manchesterSyntax.check(stranger, projectId, MARGHERITA, BARE_FRAME, BARE_FRAME,
                                                        Set.of()))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> manchesterSyntax.getCompletions(stranger, projectId, BARE_FRAME, 0, Set.of(), 20))
                .isInstanceOf(PermissionDeniedException.class);
    }

    private Set<OWLClass> parentsOfMargherita() {
        return registry.get(projectId)
                       .components()
                       .classFrameProvider()
                       .getFrame(MARGHERITA, ClassFrameTranslationOptions.defaultOptions())
                       .getParents();
    }
}
