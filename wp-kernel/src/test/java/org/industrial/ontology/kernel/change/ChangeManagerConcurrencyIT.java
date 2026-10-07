package org.industrial.ontology.kernel.change;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.api.index.RootIndex;
import org.industrial.ontology.kernel.api.lang.ActiveLanguagesManager;
import org.industrial.ontology.kernel.api.port.ChangePermissionChecker;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.project.BuiltInPrefixDeclarations;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.util.IriReplacerFactory;
import org.industrial.ontology.kernel.crud.EntityCrudContextFactory;
import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.ProjectEntityCrudKitHandlerCache;
import org.industrial.ontology.kernel.event.EventTranslatorManager;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.hierarchy.AnnotationPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.DataPropertyHierarchyProvider;
import org.industrial.ontology.kernel.hierarchy.ObjectPropertyHierarchyProvider;
import org.industrial.ontology.kernel.index.IndexUpdater;
import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.owlapi.RenameMapFactory;
import org.industrial.ontology.kernel.project.ChangeHistoryFileFactory;
import org.industrial.ontology.kernel.render.RenderingManager;
import org.industrial.ontology.kernel.project.DefaultOntologyIdManager;
import org.industrial.ontology.kernel.revision.BinaryOwlRevisionStore;
import org.industrial.ontology.kernel.revision.RevisionManager;
import org.industrial.ontology.kernel.shortform.DictionaryManager;
import org.industrial.ontology.kernel.shortform.DictionaryUpdatesProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Locking of {@link ChangeManager#applyChanges} (docs/01 §3.4, P0-08): change generation is serialised by the
 * change-processing lock, so no generator sees a project state that another writer is about to change, and the
 * project-wide write lock waits for readers holding the read lock.
 * <p>
 * Revisions go through a real {@link RevisionManager} and {@link BinaryOwlRevisionStore}; the remaining
 * collaborators are not involved in locking and are mocked.
 */
public class ChangeManagerConcurrencyIT {

    private static final int WRITERS = 100;

    private static final UserId USER = UserId.getUserId("tester");

    @TempDir
    Path tempDir;

    private final OWLDataFactory dataFactory = new OWLDataFactoryImpl();

    private final OWLOntologyID ontologyId = new OWLOntologyID(IRI.create("http://example.org/ont"));

    private ProjectId projectId;

    private ChangeHistoryFileFactory changeHistoryFileFactory;

    private ExecutorService serializationExecutor;

    private ExecutorService writers;

    private ReentrantReadWriteLock projectLock;

    private BinaryOwlRevisionStore revisionStore;

    private RevisionManager revisionManager;

    private ChangeManager changeManager;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setUp() {
        projectId = ProjectId.get(UUID.randomUUID().toString());
        changeHistoryFileFactory = mock(ChangeHistoryFileFactory.class);
        when(changeHistoryFileFactory.getChangeHistoryFile(projectId))
                .thenReturn(tempDir.resolve("change-data.binary").toFile());
        serializationExecutor = Executors.newFixedThreadPool(4);
        writers = Executors.newFixedThreadPool(16);
        projectLock = new ReentrantReadWriteLock();
        revisionStore = createRevisionStore();
        revisionManager = new RevisionManager(revisionStore);

        var rootIndex = mock(RootIndex.class);
        when(rootIndex.getEffectiveChanges(any())).thenAnswer(
                invocation -> ImmutableList.copyOf((List<OntologyChange>) invocation.getArgument(0)));
        var prefixDeclarationsStore = mock(PrefixDeclarationsStore.class);
        when(prefixDeclarationsStore.find(projectId)).thenReturn(PrefixDeclarations.get(projectId));
        var crudKitHandlerCache = mock(ProjectEntityCrudKitHandlerCache.class);
        when(crudKitHandlerCache.getHandler()).thenReturn((EntityCrudKitHandler) mock(EntityCrudKitHandler.class));

        changeManager = new ChangeManager(projectId,
                                          projectLock,
                                          dataFactory,
                                          mock(DictionaryUpdatesProcessor.class),
                                          mock(ActiveLanguagesManager.class),
                                          ChangePermissionChecker.ALLOW_ALL,
                                          prefixDeclarationsStore,
                                          mock(ProjectEventManager.class),
                                          () -> new EventTranslatorManager(Set.of()),
                                          crudKitHandlerCache,
                                          revisionManager,
                                          rootIndex,
                                          mock(DictionaryManager.class),
                                          mock(ClassHierarchyProvider.class),
                                          mock(ObjectPropertyHierarchyProvider.class),
                                          mock(DataPropertyHierarchyProvider.class),
                                          mock(AnnotationPropertyHierarchyProvider.class),
                                          mock(EntityCrudContextFactory.class),
                                          new RenameMapFactory(() -> dataFactory, () -> mock(RenderingManager.class)),
                                          new BuiltInPrefixDeclarations(ImmutableList.of()),
                                          mock(IndexUpdater.class),
                                          mock(DefaultOntologyIdManager.class),
                                          new IriReplacerFactory(() -> dataFactory));
    }

    @AfterEach
    public void tearDown() {
        writers.shutdownNow();
        revisionStore.dispose();
        serializationExecutor.shutdownNow();
    }

    @Test
    public void shouldSerialiseConcurrentWritersWithoutLosingRevisions() throws Exception {
        var start = new CountDownLatch(1);
        var results = new ArrayList<Future<?>>();
        for(int i = 0; i < WRITERS; i++) {
            results.add(writers.submit(() -> {
                start.await();
                return changeManager.applyChanges(USER, new HeadReadingChangeListGenerator());
            }));
        }
        start.countDown();
        for(var result : results) {
            result.get(30, TimeUnit.SECONDS);
        }

        var revisions = revisionManager.getRevisions();
        assertThat(revisions.size(), is(WRITERS));
        for(int i = 0; i < WRITERS; i++) {
            var revision = revisions.get(i);
            assertThat(revision.getRevisionNumber().getValue(), is(i + 1L));
            // Each generator saw exactly the head left by the previous writer.
            assertThat(declaredClass(revision), is(markerFor(i)));
        }

        revisionStore.dispose();
        var reloaded = createRevisionStore();
        reloaded.load();
        assertThat(reloaded.getRevisions(), is(equalTo(revisions)));
    }

    @Test
    public void shouldWaitForReadersBeforeApplyingChanges() throws Exception {
        Future<?> write;
        projectLock.readLock().lock();
        try {
            write = writers.submit(() -> changeManager.applyChanges(USER, new HeadReadingChangeListGenerator()));
            waitUntilWriterQueuesOnProjectLock();
            assertThrows(TimeoutException.class, () -> write.get(200, TimeUnit.MILLISECONDS));
            assertThat(revisionManager.getRevisions(), is(empty()));
        } finally {
            projectLock.readLock().unlock();
        }
        write.get(30, TimeUnit.SECONDS);
        assertThat(revisionManager.getRevisions().size(), is(1));
    }

    private void waitUntilWriterQueuesOnProjectLock() throws InterruptedException {
        var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while(!projectLock.hasQueuedThreads()) {
            if(System.nanoTime() > deadline) {
                throw new AssertionError("The writer never waited for the project write lock");
            }
            Thread.sleep(10);
        }
    }

    private BinaryOwlRevisionStore createRevisionStore() {
        return new BinaryOwlRevisionStore(projectId, changeHistoryFileFactory, dataFactory,
                                          new OntologyChangeRecordTranslatorImpl(), serializationExecutor);
    }

    private static IRI markerFor(long head) {
        return IRI.create("http://example.org/after-" + head);
    }

    private static IRI declaredClass(Revision revision) {
        var changes = revision.getChanges();
        assertThat(changes.size(), is(1));
        return ((OWLDeclarationAxiom) changes.get(0).getAxiomOrThrow()).getEntity().getIRI();
    }

    /**
     * Declares a class named after the head revision it observed, standing in for generators whose changes depend
     * on the current project state.
     */
    private class HeadReadingChangeListGenerator implements ChangeListGenerator<Boolean> {

        @Override
        public OntologyChangeList<Boolean> generateChanges(ChangeGenerationContext context) {
            var head = revisionManager.getCurrentRevision().getValue();
            // Give another writer the chance to interleave if generation were not serialised.
            Thread.yield();
            var builder = OntologyChangeList.<Boolean>builder();
            builder.add(AddAxiomChange.of(ontologyId,
                                          dataFactory.getOWLDeclarationAxiom(dataFactory.getOWLClass(markerFor(head)))));
            return builder.build(true);
        }

        @Override
        public Boolean getRenamedResult(Boolean result, RenameMap renameMap) {
            return result;
        }

        @Override
        public String getMessage(ChangeApplicationResult<Boolean> result) {
            return "Declared marker class";
        }
    }
}
