package org.industrial.ontology.kernel.revision;



import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Interners;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslator;
import org.industrial.ontology.kernel.project.ChangeHistoryFileFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.binaryowl.BinaryOWLOntologyChangeLog;
import org.semanticweb.binaryowl.chunk.SkipSetting;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.io.BufferedInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import org.industrial.ontology.kernel.api.revision.Revision;

import static org.industrial.ontology.kernel.revision.RevisionSerializationVocabulary.DESCRIPTION_META_DATA_ATTRIBUTE;
import static org.industrial.ontology.kernel.revision.RevisionSerializationVocabulary.REVISION_META_DATA_ATTRIBUTE;
import static org.industrial.ontology.kernel.revision.RevisionSerializationVocabulary.USERNAME_METADATA_ATTRIBUTE;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionStoreImpl}.
 * <p>
 * The legacy class wrote revisions on a single-thread executor of its own, which kept the appends to
 * {@code change-data.binary} in order. Revisions are now written on an executor shared by all projects
 * (wp-app's {@code KernelExecutors}), so each store chains its writes to keep them in revision order, and
 * {@link #dispose()} waits for those writes instead of shutting the shared executor down.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 29/05/15
 */
public class BinaryOwlRevisionStore implements RevisionStore {

    private static final Logger logger = LoggerFactory.getLogger(BinaryOwlRevisionStore.class);

    /** Upper bound on {@link #dispose()} so that a stalled shared executor cannot hang project shutdown. */
    private static final Duration PENDING_WRITES_TIMEOUT = Duration.ofMinutes(1);

    private final Executor serializationExecutor;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final ChangeHistoryFileFactory changeHistoryFileFactory;

    @Nonnull
    private final OWLDataFactory dataFactory;

    private ImmutableList<Revision> revisions = ImmutableList.of();

    @Nonnull
    private final OntologyChangeRecordTranslator changeRecordTranslator;

    /** Tail of this store's write chain; guarded by {@link #writeLock}. */
    private CompletableFuture<Void> pendingWrites = CompletableFuture.completedFuture(null);

    private Runnable savedHook = () -> {};

    public BinaryOwlRevisionStore(@Nonnull ProjectId projectId,
                                  @Nonnull ChangeHistoryFileFactory changeHistoryFileFactory,
                                  @Nonnull OWLDataFactory dataFactory,
                                  @Nonnull OntologyChangeRecordTranslator changeRecordTranslator,
                                  @Nonnull Executor serializationExecutor) {
        this.projectId = checkNotNull(projectId);
        this.changeHistoryFileFactory = checkNotNull(changeHistoryFileFactory);
        this.dataFactory = checkNotNull(dataFactory);
        this.changeRecordTranslator = checkNotNull(changeRecordTranslator);
        this.serializationExecutor = checkNotNull(serializationExecutor);
    }

    public void setSavedHook(Runnable savedHook) {
        this.savedHook = checkNotNull(savedHook);
    }

    @Nonnull
    @Override
    public Optional<Revision> getRevision(@Nonnull RevisionNumber revisionNumber) {
        if(revisions.isEmpty()) {
            return Optional.empty();
        }
        int index = getRevisionIndexForRevision(revisionNumber);
        if(index < 0 || revisions.size() <= index) {
            return Optional.empty();
        }
        else {
            return Optional.of(revisions.get(index));
        }
    }

    private int getRevisionIndexForRevision(RevisionNumber revision) {
        try {
            readLock.lock();
            if(revisions.isEmpty()) {
                return -1;
            }
            if(revision.isHead()) {
                return revisions.size() - 1;
            }
            var firstRevision = revisions.get(0);
            if(revision.compareTo(firstRevision.getRevisionNumber()) < 0) {
                return -1;
            }
            var lastRevision = revisions.get(revisions.size() - 1);
            if(lastRevision.getRevisionNumber() == revision) {
                return revisions.size() - 1;
            }
            var dummyRevision = Revision.createEmptyRevisionWithRevisionNumber(revision);
            return Collections.binarySearch(revisions, dummyRevision);
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    @Override
    public ImmutableList<Revision> getRevisions() {
        try {
            readLock.lock();
            return revisions;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void append(@Nonnull Revision revision) {
        checkNotNull(revision);
        try {
            writeLock.lock();
            if(revision.getRevisionNumber().compareTo(getHead()) <= 0) {
                throw new IllegalArgumentException(String.format("Revision number (%d) must be greater than the current revision number (%d)", revision
                        .getRevisionNumber()
                        .getValue(), getHead().getValue()));
            }
            var extendedListBuilder = ImmutableList.<Revision>builder();
            extendedListBuilder.addAll(revisions);
            extendedListBuilder.add(revision);
            revisions = extendedListBuilder.build();
            persistChanges(revision);
        } finally {
            writeLock.unlock();
        }

    }

    @Nonnull
    @Override
    public RevisionNumber getHead() {
        try {
            readLock.lock();
            if(revisions.isEmpty()) {
                return RevisionNumber.getRevisionNumber(0);
            }
            return revisions.get(revisions.size() - 1).getRevisionNumber();
        } finally {
            readLock.unlock();
        }

    }

    private void persistChanges(Revision revision) {
        try {
            writeLock.lock();
            var changeHistoryFile = changeHistoryFileFactory.getChangeHistoryFile(projectId);
            var revisionSerializationTask = new RevisionSerializationTask(changeHistoryFile, revision);
            revisionSerializationTask.setSavedHook(savedHook);
            if(revisions.size() != 1) {
                pendingWrites = pendingWrites
                        .thenRunAsync(() -> serialize(revisionSerializationTask, revision), serializationExecutor)
                        .exceptionally(e -> {
                            logger.error("{} Could not schedule saving revision {}.  Cause: {}", projectId, revision
                                    .getRevisionNumber().getValue(), e.getMessage(), e);
                            return null;
                        });
            }
            else {
                // Save immediately
                logger.info("{} Saving first revision of project", projectId);
                serialize(revisionSerializationTask, revision);
            }
        } finally {
            writeLock.unlock();
        }
    }

    private void serialize(RevisionSerializationTask task, Revision revision) {
        try {
            task.call();
        } catch(IOException e) {
            logger.error("{} An error occurred whilst saving revision {}.  Cause: {}.", projectId, revision
                    .getRevisionNumber().getValue(), e.getMessage(), e);
        }
    }

    @Override
    public void load() {
        try {
            writeLock.lock();
            var changeHistoryFile = changeHistoryFileFactory.getChangeHistoryFile(projectId);
            if(!changeHistoryFile.exists()) {
                changeHistoryFile.getParentFile().mkdirs();
                return;
            }
            var revisionsBuilder = ImmutableList.<Revision>builder();
            var metadataInterner = Interners.<String>newStrongInterner();
            var userIdInterner = Interners.<UserId>newStrongInterner();

            try {
                logger.info("{} Loading change history", projectId);
                var stopwatch = Stopwatch.createStarted();
                var changeLog = new BinaryOWLOntologyChangeLog();
                var inputStream = new BufferedInputStream(new FileInputStream(changeHistoryFile));
                changeLog.readChanges(inputStream, dataFactory, (changeRecordList, skipSetting, l) -> {
                    var metadata = changeRecordList.getMetadata();
                    var userName = metadataInterner.intern(metadata.getStringAttribute(USERNAME_METADATA_ATTRIBUTE.getVocabularyName(), ""));
                    var revisionNumberValue = metadata.getLongAttribute(REVISION_META_DATA_ATTRIBUTE.getVocabularyName(), 0L);
                    var revisionNumber = RevisionNumber.getRevisionNumber(revisionNumberValue);
                    var description = metadata.getStringAttribute(DESCRIPTION_META_DATA_ATTRIBUTE.getVocabularyName(), "");
                    var userId = userIdInterner.intern(UserId.getUserId(userName));

                    var internedChangeRecords = changeRecordList.getChangeRecords()
                            .stream()
                            .map(changeRecordTranslator::getOntologyChange)
                            .collect(toImmutableList());
                    var revision = new Revision(userId, revisionNumber, internedChangeRecords, changeRecordList.getTimestamp(), description);
                    revisionsBuilder.add(revision);
                }, SkipSetting.SKIP_NONE);
                inputStream.close();
                stopwatch.stop();
                revisions = revisionsBuilder.build();
                logger.info("{} Change history loading complete.  Loaded {} revisions in {} ms.", projectId, revisions.size(), stopwatch
                        .elapsed(TimeUnit.MILLISECONDS));

            } catch(Exception e) {
                logger.error("{} Failed to load change history for project.  Cause: {}", projectId, e.getMessage(), e);
            }
        } finally {
            writeLock.unlock();
        }


    }

    /**
     * Waits for the revisions appended so far to reach the change history file. The shared executor is left running.
     */
    @Override
    public void dispose() {
        CompletableFuture<Void> writes;
        try {
            writeLock.lock();
            writes = pendingWrites;
        } finally {
            writeLock.unlock();
        }
        try {
            writes.get(PENDING_WRITES_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("{} Interrupted while waiting for revisions to be saved", projectId);
        } catch(TimeoutException e) {
            logger.error("{} Revisions were not saved within {}", projectId, PENDING_WRITES_TIMEOUT);
        } catch(ExecutionException e) {
            // Unreachable: failures are logged and recovered in persistChanges.
            logger.error("{} Saving revisions failed.  Cause: {}", projectId, e.getMessage(), e);
        }
    }
}
