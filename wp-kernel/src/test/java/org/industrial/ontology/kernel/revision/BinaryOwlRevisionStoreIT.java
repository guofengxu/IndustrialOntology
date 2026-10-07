package org.industrial.ontology.kernel.revision;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslatorImpl;
import org.industrial.ontology.kernel.project.ChangeHistoryFileFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.semanticweb.owlapi.apibinding.OWLFunctionalSyntaxFactory.SubClassOf;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionStoreImpl_IT}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BinaryOwlRevisionStoreIT {

    @TempDir
    public Path temporaryFolder;

    private BinaryOwlRevisionStore store;

    private ExecutorService serializationExecutor;

    private OWLOntologyID ontologyId;

    private OWLAxiom axiom;

    private File changeHistoryFile;

    private ProjectId projectId;

    private OWLDataFactoryImpl dataFactory;

    private OntologyChangeRecordTranslatorImpl changeRecordTranslator;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Mock
    private ChangeHistoryFileFactory changeHistoryFileFactory;

    @BeforeEach
    public void setUp() throws IOException {
        projectId = ProjectId.get(UUID.randomUUID().toString());
        changeHistoryFile = Files.createTempFile(temporaryFolder, "junit", ".tmp").toFile();
        when(changeHistoryFileFactory.getChangeHistoryFile(projectId)).thenReturn(changeHistoryFile);
        dataFactory = new OWLDataFactoryImpl();
        changeRecordTranslator = new OntologyChangeRecordTranslatorImpl();
        ontologyId = new OWLOntologyID(IRI.create("http://example.org/OntA"));
        var clsA = dataFactory.getOWLClass(IRI.create("http://example.org/A"));
        var clsB = dataFactory.getOWLClass(IRI.create("http://example.org/A"));
        axiom = SubClassOf(clsA, clsB);
        serializationExecutor = Executors.newFixedThreadPool(4);
        store = new BinaryOwlRevisionStore(projectId, changeHistoryFileFactory, dataFactory, changeRecordTranslator, serializationExecutor);
    }

    @Test
    public void shouldAddRevision() {
        var revision = createRevision();
        store.append(revision);
        var revisions = store.getRevisions();
        assertThat(revisions, contains(revision));
    }

    @Test
    public void shouldHaveZeroRevisionNumberAtStart() {
        var revisionNumber = store.getHead();
        assertThat(revisionNumber.getValue(), is(0L));
    }

    @Test
    public void shouldIncrementCurrentRevisionNumber() {
        var revision = createRevision();
        store.append(revision);
        var revisionNumber = store.getHead();
        assertThat(revisionNumber.getValue(), is(1L));
    }

    @Test
    public void shouldThrowIllegalArgumentIfAddedRevisionNumberIsEqualToCurrentRevisionNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            var revision = createRevision();
            store.append(revision);
            store.append(revision);
        });
    }

    @Test
    public void shouldThrowIllegalArgumentIfAddedRevisionNumberIsLessThanCurrentRevisionNumber() {
        assertThrows(IllegalArgumentException.class, () -> {
            var revision = createRevision();
            store.append(revision);
            var smallerRevision = createRevision(RevisionNumber.getRevisionNumber(0));
            store.append(smallerRevision);
        });
    }

    @Test
    public void shouldSaveFirstRevisionImmediately() {
        var revision = createRevision();
        assertThat(changeHistoryFile.length(), is(0L));
        store.append(revision);
        assertThat(changeHistoryFile.length(), is(greaterThan(0L)));
    }

    @Test
    public synchronized void shouldSaveSubsequentRevisions() throws InterruptedException {
        store.append(createRevision(RevisionNumber.getRevisionNumber(1)));
        var initialLength = changeHistoryFile.length();
        // Add hook to be notified of when the save has taken place
        store.setSavedHook(() -> countDownLatch.countDown());
        store.append(createRevision(RevisionNumber.getRevisionNumber(2)));
        // Wait until it has been saved
        countDownLatch.await();
        var nextLength = changeHistoryFile.length();
        assertThat(nextLength, is(greaterThan(initialLength)));
    }

    @Test
    public void shouldGetRevision() {
        var revision = createRevision();
        store.append(revision);
        var retrievedRevision = store.getRevision(RevisionNumber.getRevisionNumber(1));
        assertThat(retrievedRevision, is(equalTo(Optional.of(revision))));
    }

    @Test
    public void shouldNotGetRevision() {
        var revision = store.getRevision(RevisionNumber.getRevisionNumber(1));
        assertThat(revision.isEmpty(), is(true));
    }

    @Test
    public void shouldLoadSavedRevision() {
        var revision = createRevision();
        store.append(revision);
        var otherStore = createStore();
        otherStore.load();
        // The legacy test asserted on the original store, so reloading was never checked.
        var revisions = otherStore.getRevisions();
        assertThat(revisions, contains(revision));
        otherStore.dispose();
    }

    @Test
    public void shouldPersistRevisionsInOrderOnSharedMultiThreadedExecutor() {
        var appended = new ArrayList<Revision>();
        for(int i = 1; i <= 50; i++) {
            var revision = createRevision(RevisionNumber.getRevisionNumber(i));
            store.append(revision);
            appended.add(revision);
        }
        store.dispose();
        var reloadedStore = createStore();
        reloadedStore.load();
        assertThat(reloadedStore.getRevisions(), is(equalTo(appended)));
        assertThat(reloadedStore.getHead().getValue(), is(50L));
        reloadedStore.dispose();
    }

    @Test
    public void shouldNotShutDownSharedExecutorOnDispose() {
        store.append(createRevision(RevisionNumber.getRevisionNumber(1)));
        store.append(createRevision(RevisionNumber.getRevisionNumber(2)));
        store.dispose();
        assertThat(serializationExecutor.isShutdown(), is(false));
    }

    private BinaryOwlRevisionStore createStore() {
        return new BinaryOwlRevisionStore(projectId, changeHistoryFileFactory, dataFactory, changeRecordTranslator,
                                          serializationExecutor);
    }

    private Revision createRevision() {
        var revisionNumber = RevisionNumber.getRevisionNumber(1);
        return createRevision(revisionNumber);
    }

    private Revision createRevision(RevisionNumber revisionNumber) {
        var changes = ImmutableList.<OntologyChange>of(AddAxiomChange.of(ontologyId, axiom));
        var userId = UserId.getUserId("The User");
        var timestamp = System.currentTimeMillis();
        var highLevelDescription = "A change that was mad";
        return new Revision(userId, revisionNumber, changes, timestamp, highLevelDescription);
    }

    @AfterEach
    public void tearDown() throws Exception {
        store.dispose();
        serializationExecutor.shutdownNow();
    }
}
