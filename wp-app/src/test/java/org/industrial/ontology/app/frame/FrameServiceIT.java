package org.industrial.ontology.app.frame;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.ClassFrame;
import org.industrial.ontology.domain.frame.PlainAnnotationPropertyFrame;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.industrial.ontology.domain.frame.PlainDataPropertyFrame;
import org.industrial.ontology.domain.frame.PlainNamedIndividualFrame;
import org.industrial.ontology.domain.frame.PlainObjectPropertyFrame;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link FrameService} (07 5.1-1) on the pizza project.
 */
class FrameServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private FrameService frames;

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
        frames = context.bean(FrameService.class);
        registry = context.bean(ProjectRegistry.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
    }

    @Test
    void aClassFrameShouldBePlainForEditingAndRenderedForShowing() {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var view = frames.getFrame(BOB, projectId, margherita);
        assertThat(view.frame()).isInstanceOf(PlainClassFrame.class);
        var frame = (PlainClassFrame) view.frame();
        assertThat(frame.getSubject()).isEqualTo(margherita);
        assertThat(frame.getParents()).contains(PizzaOntology.cls(DATA_FACTORY, "NamedPizza"));
        assertThat(view.rendered()).isInstanceOf(ClassFrame.class);
        var rendered = (ClassFrame) view.rendered();
        assertThat(rendered.subject().getBrowserText()).isEqualTo("Margherita");
        assertThat(rendered.classEntries()).anySatisfy(parent -> assertThat(parent.getBrowserText())
                .isEqualTo("Named Pizza"));
        assertThat(rendered.propertyValues()).hasSameSizeAs(frame.getPropertyValues());
    }

    @Test
    void everyEntityTypeButDatatypesShouldHaveAFrame() {
        assertThat(frames.getFrame(ALICE, projectId, PizzaOntology.objectProperty(DATA_FACTORY, "hasTopping"))
                         .frame()).isInstanceOf(PlainObjectPropertyFrame.class);
        assertThat(frames.getFrame(ALICE, projectId,
                                   PizzaOntology.dataProperty(DATA_FACTORY, "hasCalorificContentValue")).frame())
                .isInstanceOf(PlainDataPropertyFrame.class);
        assertThat(frames.getFrame(ALICE, projectId,
                                   DATA_FACTORY.getOWLAnnotationProperty(PizzaOntology.iri("pizzaNote"))).frame())
                .isInstanceOf(PlainAnnotationPropertyFrame.class);
        var individual = frames.getFrame(ALICE, projectId,
                                         PizzaOntology.individual(DATA_FACTORY, "ExampleMargherita"));
        assertThat(individual.frame()).isInstanceOf(PlainNamedIndividualFrame.class);
        assertThat(((PlainNamedIndividualFrame) individual.frame()).getParents())
                .contains(PizzaOntology.cls(DATA_FACTORY, "Margherita"));
        assertThatThrownBy(() -> frames.getFrame(ALICE, projectId, OWL2Datatype.XSD_INTEGER.getDatatype(DATA_FACTORY)))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void anEditedFrameShouldBeAppliedInOneRevisionWithTheCommitMessage() {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var from = (PlainClassFrame) frames.getFrame(ALICE, projectId, margherita).frame();
        var to = withComment(from, "The classic");
        var headBefore = registry.get(projectId).revisionManager().getCurrentRevision();

        var outcome = frames.updateFrame(ALICE, projectId, from, to, "Commented on Margherita");

        assertThat(outcome.result()).isEqualTo(margherita);
        var revision = outcome.revision().orElseThrow();
        assertThat(revision.getRevisionNumber().getValue()).isEqualTo(headBefore.getValue() + 1);
        assertThat(revision.getUserId()).isEqualTo(ALICE);
        assertThat(revision.getHighLevelDescription()).isEqualTo("Commented on Margherita");
        assertThat(revision.getChanges()).hasSize(1);
        assertThat(outcome.eventTag().getOrdinal()).isPositive();
        assertThat(frames.getFrame(ALICE, projectId, margherita).frame()).isEqualTo(to);
    }

    @Test
    void anEditOfAFrameThatChangedSinceItWasReadShouldBeAConflict() {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var read = (PlainClassFrame) frames.getFrame(ALICE, projectId, margherita).frame();
        frames.updateFrame(ALICE, projectId, read, withComment(read, "First"), null);
        var head = registry.get(projectId).revisionManager().getCurrentRevision();

        assertThatThrownBy(() -> frames.updateFrame(ALICE, projectId, read, withComment(read, "Second"), null))
                .isInstanceOf(WpException.class)
                .satisfies(e -> {
                    assertThat(((WpException) e).getCode()).isEqualTo(FrameService.CONFLICT_FRAME_CHANGED);
                    assertThat(((WpException) e).getStatus()).isEqualTo(409);
                });

        assertThat(registry.get(projectId).revisionManager().getCurrentRevision()).isEqualTo(head);
        assertThat(frames.getFrame(ALICE, projectId, margherita).frame()).isEqualTo(withComment(read, "First"));
    }

    @Test
    void anUnchangedFrameShouldRecordNoRevision() {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var frame = frames.getFrame(ALICE, projectId, margherita).frame();
        var head = registry.get(projectId).revisionManager().getCurrentRevision();
        var outcome = frames.updateFrame(ALICE, projectId, frame, frame, null);
        assertThat(outcome.revision()).isEmpty();
        assertThat(registry.get(projectId).revisionManager().getCurrentRevision()).isEqualTo(head);
    }

    @Test
    void framesOfTwoEntitiesShouldBeRefused() {
        var margherita = frames.getFrame(ALICE, projectId, PizzaOntology.cls(DATA_FACTORY, "Margherita")).frame();
        var funghi = frames.getFrame(ALICE, projectId, PizzaOntology.cls(DATA_FACTORY, "Funghi")).frame();
        assertThatThrownBy(() -> frames.updateFrame(ALICE, projectId, margherita, funghi, null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void theFramesOfBuiltInEntitiesShouldNotBeEdited() {
        var thing = DATA_FACTORY.getOWLThing();
        var frame = (PlainClassFrame) frames.getFrame(ALICE, projectId, thing).frame();
        var withParent = PlainClassFrame.get(thing, ImmutableSet.of(PizzaOntology.cls(DATA_FACTORY, "Pizza")),
                                             frame.getPropertyValues());
        assertThatThrownBy(() -> frames.updateFrame(ALICE, projectId, frame, withParent, null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void editingShouldNeedEditOntologyAndReadingViewProject() {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var from = (PlainClassFrame) frames.getFrame(BOB, projectId, margherita).frame();
        assertThatThrownBy(() -> frames.updateFrame(BOB, projectId, from, withComment(from, "No"), null))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> frames.getFrame(UserId.getUserId("carol"), projectId, margherita))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> frames.getFrame(ALICE, ProjectId.getNil(), margherita))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void readsShouldWaitWhileTheProjectIsBeingChanged() throws Exception {
        var margherita = PizzaOntology.cls(DATA_FACTORY, "Margherita");
        var writeLock = registry.get(projectId).lock().writeLock();
        var executor = Executors.newSingleThreadExecutor();
        try {
            Future<EntityFrameView> read;
            writeLock.lock();
            try {
                read = executor.submit(() -> frames.getFrame(ALICE, projectId, margherita));
                assertThatThrownBy(() -> read.get(300, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            } finally {
                writeLock.unlock();
            }
            assertThat(read.get(10, TimeUnit.SECONDS).frame().getSubject()).isEqualTo(margherita);
        } finally {
            executor.shutdownNow();
        }
    }

    private static PlainClassFrame withComment(PlainClassFrame frame, String comment) {
        var values = ImmutableSet.<PlainPropertyValue>builder()
                                 .addAll(frame.getPropertyValues())
                                 .add(PlainPropertyAnnotationValue.get(DATA_FACTORY.getRDFSComment(),
                                                                       DATA_FACTORY.getOWLLiteral(comment, "en")))
                                 .build();
        return PlainClassFrame.get(frame.getSubject(), frame.getParents(), values);
    }
}
