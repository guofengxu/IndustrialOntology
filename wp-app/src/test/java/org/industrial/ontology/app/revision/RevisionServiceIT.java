package org.industrial.ontology.app.revision;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.AxiomsService;
import org.industrial.ontology.app.frame.FrameService;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.app.project.ProjectTestFixture;
import org.industrial.ontology.domain.change.ProjectChange;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.PlainClassFrame;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.industrial.ontology.domain.frame.PlainPropertyValue;
import org.industrial.ontology.domain.pagination.PageRequest;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;
import org.industrial.ontology.kernel.api.revision.RevisionDetails;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_EDIT;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;

/**
 * {@link RevisionService} (07 5.1-7) on the pizza project, whose first revision is the import.
 */
class RevisionServiceIT {

    private static final UserId ALICE = UserId.getUserId("alice");

    private static final UserId BOB = UserId.getUserId("bob");

    private static final UserId CAROL = UserId.getUserId("carol");

    private static final OWLDataFactory DATA_FACTORY = OWLManager.getOWLDataFactory();

    private static final OWLClass MARGHERITA = PizzaOntology.cls(DATA_FACTORY, "Margherita");

    private static final OWLClass AMERICAN = PizzaOntology.cls(DATA_FACTORY, "American");

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private RevisionService revisions;

    private FrameService frames;

    private ProjectTestFixture fixture;

    private ProjectId projectId;

    /** The head after the import: the revisions of the tests come after it. */
    private long base;

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
        revisions = context.bean(RevisionService.class);
        frames = context.bean(FrameService.class);
        projectId = fixture.createPizzaProject(ALICE, sources);
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
        base = revisions.getHeadRevision(ALICE, projectId).getValue();
    }

    /**
     * 07 S8: reverting a revision restores the frame as it was before the revision.
     */
    @Test
    void revertingTheRevisionOfAFrameEditShouldRestoreTheFrame() {
        var original = frame(MARGHERITA);
        var edited = frames.updateFrame(ALICE, projectId, original, withComment(original, "Basil on top"), null)
                           .revision().orElseThrow();
        assertThat(frame(MARGHERITA)).isNotEqualTo(original);

        var outcome = revisions.revert(ALICE, projectId, edited.getRevisionNumber(), null);

        assertThat(outcome.result()).isTrue();
        var revert = outcome.revision().orElseThrow();
        assertThat(revert.getRevisionNumber().getValue()).isEqualTo(edited.getRevisionNumber().getValue() + 1);
        assertThat(revert.getHighLevelDescription()).isEqualTo("Reverted revision "
                                                                       + edited.getRevisionNumber().getValue());
        assertThat(revert.getUserId()).isEqualTo(ALICE);
        assertThat(frame(MARGHERITA)).isEqualTo(original);
        // The edit stays in the history
        assertThat(revisions.getRevision(BOB, projectId, edited.getRevisionNumber()).details().getMsg())
                .isEqualTo(edited.getHighLevelDescription());
    }

    @Test
    void aRevertShouldTakeTheCommitMessageAndChangeNothingTheSecondTime() {
        var original = frame(MARGHERITA);
        var edited = frames.updateFrame(ALICE, projectId, original, withComment(original, "Basil"), null)
                           .revision().orElseThrow().getRevisionNumber();
        var first = revisions.revert(ALICE, projectId, edited, "Basil was a mistake");
        assertThat(first.revision().orElseThrow().getHighLevelDescription()).isEqualTo("Basil was a mistake");
        var head = revisions.getHeadRevision(ALICE, projectId);

        var second = revisions.revert(ALICE, projectId, edited, null);

        assertThat(second.revision()).isEmpty();
        assertThat(revisions.getHeadRevision(ALICE, projectId)).isEqualTo(head);
    }

    /**
     * 07 S8: the changes of an entity are only that entity's changes.
     */
    @Test
    void theChangesOfAnEntityShouldBeOnlyItsChangesNewestFirst() {
        var margherita = frame(MARGHERITA);
        var first = frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "Tomato"), null)
                          .revision().orElseThrow().getRevisionNumber();
        var american = frame(AMERICAN);
        frames.updateFrame(ALICE, projectId, american, withComment(american, "Pepperoni"), null);
        var margheritaAgain = frame(MARGHERITA);
        var second = frames.updateFrame(ALICE, projectId, margheritaAgain,
                                        withComment(margheritaAgain, "Mozzarella"), null)
                           .revision().orElseThrow().getRevisionNumber();

        var changes = revisions.getChanges(BOB, projectId, Optional.of(MARGHERITA),
                                           PageRequest.requestPageWithSize(1, 10));

        // The two edits of Margherita, newest first, then the import; not the edit of American
        var numbers = changes.getPageElements().stream().map(change -> change.getRevisionNumber().getValue()).toList();
        assertThat(numbers).startsWith(second.getValue(), first.getValue());
        assertThat(numbers.subList(2, numbers.size())).isNotEmpty().allMatch(number -> number <= base);
        assertThat(changes.getTotalElements()).isEqualTo(numbers.size());
        var lines = changes.getPageElements().get(0).getDiff().getPageElements();
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getLineElement()).contains("Mozzarella").doesNotContain("Pepperoni");
        assertThat(changes.getPageElements()).allSatisfy(change -> assertThat(diffText(change))
                .doesNotContain("Pepperoni"));

        var secondPage = revisions.getChanges(BOB, projectId, Optional.of(MARGHERITA),
                                              PageRequest.requestPageWithSize(2, 2));
        assertThat(secondPage.getPageCount()).isEqualTo((numbers.size() + 1) / 2);
        assertThat(secondPage.getTotalElements()).isEqualTo(numbers.size());
        assertThat(secondPage.getPageElements()).extracting(change -> change.getRevisionNumber().getValue())
                                                .containsExactlyElementsOf(numbers.subList(2,
                                                                                           Math.min(4, numbers.size())));
    }

    @Test
    void theChangesOfTheProjectShouldBeOneEntryPerRevisionNewestFirst() {
        var margherita = frame(MARGHERITA);
        frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "Tomato"), null);
        var american = frame(AMERICAN);
        frames.updateFrame(ALICE, projectId, american, withComment(american, "Pepperoni"), null);

        var page = revisions.getChanges(BOB, projectId, Optional.empty(), PageRequest.requestPageWithSize(1, 2));

        assertThat(page.getPageElements()).extracting(change -> change.getRevisionNumber().getValue())
                                          .containsExactly(base + 2, base + 1);
        assertThat(page.getTotalElements()).isEqualTo(base + 2);
        assertThat(page.getPageCount()).isEqualTo((int) ((base + 3) / 2));
        assertThat(diffText(page.getPageElements().get(0))).contains("Pepperoni");
        // The import, revision 1, has more changes than one entry shows
        var importChange = revisions.getChanges(BOB, projectId, Optional.empty(),
                                                PageRequest.requestPageWithSize((int) base + 2, 1))
                                    .getPageElements().get(0);
        assertThat(importChange.getRevisionNumber().getValue()).isEqualTo(1);
        assertThat(importChange.getChangeCount()).isGreaterThan(50);
        assertThat(importChange.getDiff().getPageElements()).hasSize(50);
        assertThat(importChange.getDiff().getPageCount())
                .isEqualTo((importChange.getChangeCount() + 49) / 50);
    }

    @Test
    void literalsInTheDiffShouldBeEscaped() {
        var margherita = frame(MARGHERITA);
        frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "<script>alert(1)</script>"), null);

        var change = revisions.getChanges(BOB, projectId, Optional.of(MARGHERITA),
                                          PageRequest.requestPageWithSize(1, 1)).getPageElements().get(0);

        assertThat(diffText(change)).contains("&lt;script&gt;").doesNotContain("<script>");
    }

    @Test
    void revisionsShouldBeListedByRangeAndAuthor() {
        fixture.grantProjectRoles(BOB, projectId, CAN_EDIT);
        var margherita = frame(MARGHERITA);
        frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "By Alice"), "Alice's edit");
        var american = frame(AMERICAN);
        frames.updateFrame(BOB, projectId, american, withComment(american, "By Bob"), "Bob's edit");

        var all = numbers(revisions.getRevisions(BOB, projectId, 1, null, Optional.empty()));
        assertThat(all).hasSize((int) base + 2).startsWith(1L).endsWith(base + 1, base + 2).isSorted();
        assertThat(numbers(revisions.getRevisions(BOB, projectId, base + 1, base + 1, Optional.empty())))
                .containsExactly(base + 1);
        var bobs = revisions.getRevisions(BOB, projectId, 1, null, Optional.of(BOB));
        assertThat(numbers(bobs)).containsExactly(base + 2);
        assertThat(bobs.get(0).getDescription()).isEqualTo("Bob's edit");
        assertThat(bobs.get(0).getChangeCount()).isEqualTo(1);
        assertThat(revisions.getRevisions(BOB, projectId, base + 3, null, Optional.empty())).isEmpty();
        assertThat(revisions.getHeadRevision(BOB, projectId)).isEqualTo(RevisionNumber.getRevisionNumber(base + 2));

        assertThatThrownBy(() -> revisions.getRevisions(BOB, projectId, 0, null, Optional.empty()))
                .isInstanceOf(WpException.class).extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> revisions.getRevisions(BOB, projectId, 3, 2L, Optional.empty()))
                .isInstanceOf(WpException.class).extracting("code").isEqualTo("INVALID_REQUEST");
    }

    @Test
    void aRevisionShouldHaveItsAxiomChangesAndSubjects() {
        var margherita = frame(MARGHERITA);
        var edit = frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "Basil"), "Basil")
                         .revision().orElseThrow();

        var listed = revisions.getRevision(BOB, projectId, edit.getRevisionNumber());
        RevisionDetails details = listed.details();
        assertThat(listed.changeCount()).isOne();

        assertThat(details.getRevisionNumber()).isEqualTo(edit.getRevisionNumber().getValue());
        assertThat(details.getUserId()).isEqualTo(ALICE);
        assertThat(details.getMsg()).isEqualTo("Basil");
        assertThat(details.getSubjects()).containsExactly(MARGHERITA.getIRI());
        assertThat(details.getChangeDetails()).singleElement().satisfies(change -> {
            assertThat(change.getOperation()).isEqualTo(RevisionDetails.ChangeOperation.ADD);
            assertThat(change.getAxiom().toString()).contains("Basil");
        });

        assertThatThrownBy(() -> revisions.getRevision(BOB, projectId, RevisionNumber.getRevisionNumber(99)))
                .isInstanceOf(WpException.class)
                .satisfies(e -> {
                    assertThat(((WpException) e).getCode()).isEqualTo(RevisionService.REVISION_NOT_FOUND);
                    assertThat(((WpException) e).getStatus()).isEqualTo(404);
                });
        assertThatThrownBy(() -> revisions.revert(ALICE, projectId, RevisionNumber.getRevisionNumber(99), null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo(RevisionService.REVISION_NOT_FOUND);
        // The head alias names whichever revision is the head when it is resolved
        assertThatThrownBy(() -> revisions.revert(ALICE, projectId, RevisionNumber.getHeadRevisionNumber(), null))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> revisions.getRevision(BOB, projectId, RevisionNumber.getHeadRevisionNumber()))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo("INVALID_REQUEST");

        // The import lists all its axioms, below the limit; its changes also add the ontology's annotations
        var imported = revisions.getRevision(BOB, projectId, RevisionNumber.getRevisionNumber(1));
        assertThat(imported.changeCount())
                .isEqualTo(PizzaOntology.AXIOM_COUNT + PizzaOntology.ONTOLOGY_ANNOTATION_COUNT);
        assertThat(imported.details().getChangeDetails()).hasSize(PizzaOntology.AXIOM_COUNT);
    }

    @Test
    void aLargeRevisionShouldListItsFirstChangesAndCountThemAll() {
        var document = new StringBuilder("Ontology(\n");
        for (var i = 0; i < RevisionService.MAX_LISTED_CHANGES + 1; i++) {
            document.append("SubClassOf(<%s> <%s>)\n".formatted(PizzaOntology.iri("Generated" + i),
                                                                PizzaOntology.iri("Pizza")));
        }
        document.append(")");
        var revision = context.bean(AxiomsService.class)
                              .addAxioms(ALICE, projectId, document.toString(), AxiomsService.Syntax.FUNCTIONAL, null)
                              .revision().orElseThrow();

        var listed = revisions.getRevision(BOB, projectId, revision.getRevisionNumber());

        assertThat(listed.changeCount()).isEqualTo(revision.getSize())
                                        .isGreaterThan(RevisionService.MAX_LISTED_CHANGES);
        assertThat(listed.details().getChangeDetails()).hasSize(RevisionService.MAX_LISTED_CHANGES);
        assertThat(listed.details().getSubjects()).isNotEmpty()
                                                  .hasSizeLessThanOrEqualTo(RevisionService.MAX_LISTED_CHANGES);
    }

    /**
     * 05 P1-08: the diff between two revisions lists the axioms added and removed overall, in Manchester syntax.
     */
    @Test
    void theDiffShouldBeTheAxiomsAddedAndRemovedOverall() {
        var margherita = frame(MARGHERITA);
        var basil = frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "Basil"), null)
                          .revision().orElseThrow().getRevisionNumber();
        var american = frame(AMERICAN);
        frames.updateFrame(ALICE, projectId, american, withComment(american, "Pepperoni"), null);
        revisions.revert(ALICE, projectId, basil, null);

        var overall = revisions.getDiff(BOB, projectId, base, null);
        assertThat(overall.from()).isEqualTo(base);
        assertThat(overall.to()).isEqualTo(base + 3);
        assertThat(overall.addedAxioms()).singleElement().asString().contains("American").contains("Pepperoni");
        assertThat(overall.removedAxioms()).isEmpty();
        assertThat(overall.addedCount()).isOne();

        var first = revisions.getDiff(BOB, projectId, base, basil.getValue());
        assertThat(first.addedAxioms()).singleElement().asString().contains("Margherita").contains("Basil");

        var later = revisions.getDiff(BOB, projectId, basil.getValue(), null);
        assertThat(later.removedAxioms()).singleElement().asString().contains("Basil");
        assertThat(later.addedAxioms()).singleElement().asString().contains("Pepperoni");

        var everything = revisions.getDiff(BOB, projectId, 0, null);
        assertThat(everything.addedCount()).isGreaterThan(PizzaOntology.AXIOM_COUNT - 10);
        assertThat(everything.removedCount()).isZero();

        assertThatThrownBy(() -> revisions.getDiff(BOB, projectId, -1, null))
                .isInstanceOf(WpException.class).extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> revisions.getDiff(BOB, projectId, 2, 1L))
                .isInstanceOf(WpException.class).extracting("code").isEqualTo("INVALID_REQUEST");
        assertThatThrownBy(() -> revisions.getDiff(BOB, projectId, 0, base + 4))
                .isInstanceOf(WpException.class).extracting("code").isEqualTo(RevisionService.REVISION_NOT_FOUND);
        assertThatThrownBy(() -> revisions.getDiff(CAROL, projectId, 0, null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void readingShouldNeedViewChangesAndRevertingRevertChangesAndEditOntology() {
        var margherita = frame(MARGHERITA);
        var edit = frames.updateFrame(ALICE, projectId, margherita, withComment(margherita, "Basil"), null)
                         .revision().orElseThrow().getRevisionNumber();

        // A viewer reads the history but does not revert
        assertThat(revisions.getHeadRevision(BOB, projectId)).isEqualTo(edit);
        assertThatThrownBy(() -> revisions.revert(BOB, projectId, edit, null))
                .isInstanceOf(PermissionDeniedException.class);
        // Without a role, nothing
        assertThatThrownBy(() -> revisions.getRevisions(CAROL, projectId, 1, null, Optional.empty()))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> revisions.getHeadRevision(CAROL, projectId))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> revisions.getRevision(CAROL, projectId, edit))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> revisions.getChanges(CAROL, projectId, Optional.empty(),
                                                      PageRequest.requestFirstPage()))
                .isInstanceOf(PermissionDeniedException.class);
        assertThatThrownBy(() -> revisions.getHeadRevision(ALICE, ProjectId.getNil()))
                .isInstanceOf(ProjectNotFoundException.class);
        assertThat(frame(MARGHERITA)).isNotEqualTo(margherita);
    }

    @Test
    void theChangesShouldBeReadConcurrently() throws Exception {
        var edited = new ArrayList<OWLClass>();
        for (var name : List.of("Margherita", "American", "AmericanHot", "Capricciosa", "Diavola", "Funghi")) {
            var cls = PizzaOntology.cls(DATA_FACTORY, name);
            var from = frame(cls);
            frames.updateFrame(ALICE, projectId, from, withComment(from, name + " note"), null);
            edited.add(cls);
        }
        var executor = Executors.newFixedThreadPool(edited.size());
        try {
            var tasks = new ArrayList<Callable<List<Long>>>();
            for (var cls : edited) {
                tasks.add(() -> revisions.getChanges(BOB, projectId, Optional.of(cls),
                                                     PageRequest.requestPageWithSize(1, 10))
                                         .getPageElements().stream()
                                         .map(change -> change.getRevisionNumber().getValue())
                                         .toList());
            }
            var results = new ArrayList<List<Long>>();
            for (var future : executor.invokeAll(tasks)) {
                results.add(future.get());
            }
            for (var i = 0; i < edited.size(); i++) {
                // The entity's edit, then the import
                assertThat(results.get(i).get(0)).isEqualTo(base + 1 + i);
                assertThat(results.get(i).subList(1, results.get(i).size())).isNotEmpty()
                                                                            .allMatch(number -> number <= base);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private PlainClassFrame frame(OWLClass cls) {
        return (PlainClassFrame) frames.getFrame(ALICE, projectId, cls).frame();
    }

    private static List<Long> numbers(List<RevisionSummary> summaries) {
        return summaries.stream().map(summary -> summary.getRevisionNumber().getValue()).toList();
    }

    private static String diffText(ProjectChange change) {
        var text = new StringBuilder();
        change.getDiff().getPageElements().forEach(line -> text.append(line.getLineElement()).append('\n'));
        return text.toString();
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
