package org.industrial.ontology.app.project;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Striped;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.access.ProjectResource;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.kernel.io.download.DownloadFormat;
import org.industrial.ontology.kernel.io.download.ProjectDownloadCache;
import org.industrial.ontology.kernel.io.download.ProjectDownloader;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.revision.HeadRevisionNumberFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.DOWNLOAD_PROJECT;

/**
 * Project downloads (docs/02 §3 {@code /download}): the legacy {@code ProjectDownloadService} with its
 * {@code CreateDownloadTask}. A download is a zip archive of the revision's ontologies in one format, created once
 * in the download cache and served from there.
 * <p>
 * As in the legacy service, a project's downloads are created one at a time, and only one download is created at a
 * time in the whole application (the legacy generator was single-threaded), which bounds the memory it takes. Unlike
 * the legacy task, a download is written to a temporary file and moved into the cache when complete, so a failure
 * never leaves a broken archive in the cache, and it is built from the head revision's number rather than "head",
 * so it cannot contain a revision appended meanwhile.
 */
public class ProjectDownloadService {

    /**
     * The requested revision is not one of the project's.
     */
    public static final String REVISION_NOT_FOUND = "REVISION_NOT_FOUND";

    private static final Logger logger = LoggerFactory.getLogger(ProjectDownloadService.class);

    private final AccessManager accessManager;

    private final MongoProjectDetailsRepository projectDetailsRepository;

    private final MongoPrefixDeclarationsStore prefixDeclarationsStore;

    private final ProjectRegistry projectRegistry;

    private final ProjectDownloadCache downloadCache;

    private final HeadRevisionNumberFinder headRevisionNumberFinder;

    private final Striped<Lock> projectLocks = Striped.lazyWeakLock(10);

    private final Semaphore downloadCreation = new Semaphore(1, true);

    public ProjectDownloadService(@Nonnull AccessManager accessManager,
                                  @Nonnull MongoProjectDetailsRepository projectDetailsRepository,
                                  @Nonnull MongoPrefixDeclarationsStore prefixDeclarationsStore,
                                  @Nonnull ProjectRegistry projectRegistry,
                                  @Nonnull DataDirectoryLayout dataDirectoryLayout) {
        this.accessManager = checkNotNull(accessManager);
        this.projectDetailsRepository = checkNotNull(projectDetailsRepository);
        this.prefixDeclarationsStore = checkNotNull(prefixDeclarationsStore);
        this.projectRegistry = checkNotNull(projectRegistry);
        this.downloadCache = new ProjectDownloadCache(dataDirectoryLayout.getDownloadCacheDirectorySupplier());
        this.headRevisionNumberFinder = new HeadRevisionNumberFinder(dataDirectoryLayout.getChangeHistoryFileFactory());
    }

    /**
     * The download of the revision in the format, created if the cache does not have it yet.
     *
     * @param revision a revision number, or {@link RevisionNumber#getHeadRevisionNumber()} for the latest
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not download the project
     * @throws WpException               {@value #REVISION_NOT_FOUND} (404) for a revision after the head or below 0
     */
    @Nonnull
    public ProjectDownload download(@Nonnull UserId caller,
                                    @Nonnull ProjectId projectId,
                                    @Nonnull RevisionNumber revision,
                                    @Nonnull DownloadFormat format) {
        var details = projectDetailsRepository.findOne(projectId)
                                              .orElseThrow(() -> new ProjectNotFoundException(projectId));
        accessManager.require(caller, ProjectResource.of(projectId), DOWNLOAD_PROJECT);
        var head = getHeadRevisionNumber(projectId);
        var realRevision = revision.isHead() ? head : revision;
        if (realRevision.getValue() < 0 || realRevision.compareTo(head) > 0) {
            throw new WpException(REVISION_NOT_FOUND, 404, "Project " + projectId.getId() + " has no revision "
                    + realRevision.getValue());
        }
        var path = downloadCache.getCachedDownloadPath(projectId, realRevision, format);
        try {
            createDownloadIfNecessary(caller, details, realRevision, format, path);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create the download of project " + projectId, e);
        }
        return new ProjectDownload(path, getClientSideFileName(details, revision, format));
    }

    private void createDownloadIfNecessary(UserId requester,
                                           ProjectDetails details,
                                           RevisionNumber revision,
                                           DownloadFormat format,
                                           Path downloadPath) throws IOException {
        var projectId = details.getProjectId();
        var lock = projectLocks.get(projectId);
        lock.lock();
        try {
            if (Files.exists(downloadPath)) {
                logger.info("{} {} Download for the requested revision already exists.  Using cached download.",
                            projectId, requester);
                return;
            }
            downloadCreation.acquireUninterruptibly();
            try {
                var stopwatch = Stopwatch.createStarted();
                var downloader = new ProjectDownloader(projectId,
                                                       details.getDisplayName(),
                                                       revision,
                                                       format,
                                                       projectRegistry.get(projectId).revisionManager(),
                                                       prefixDeclarationsStore);
                Files.createDirectories(downloadPath.getParent());
                var partial = Files.createTempFile(downloadPath.getParent(), "download-", ".part");
                try {
                    try (var outputStream = new BufferedOutputStream(Files.newOutputStream(partial))) {
                        downloader.writeProject(outputStream);
                    }
                    Files.move(partial, downloadPath, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(partial);
                }
                logger.info("{} {} Created download ({} bytes) in {} ms", projectId, requester,
                            Files.size(downloadPath), stopwatch.elapsed(TimeUnit.MILLISECONDS));
            } finally {
                downloadCreation.release();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * From the loaded project if it is loaded; otherwise read from its change history, as the legacy service did,
     * so that a download that is in the cache does not load the project.
     */
    private RevisionNumber getHeadRevisionNumber(ProjectId projectId) {
        var loaded = projectRegistry.getIfLoaded(projectId);
        if (loaded.isPresent()) {
            return loaded.get().revisionManager().getCurrentRevision();
        }
        try {
            return headRevisionNumberFinder.getHeadRevisionNumber(projectId);
        } catch (NoSuchFileException e) {
            // A project that has never been changed has no change history yet
            return RevisionNumber.getRevisionNumber(0);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the change history of project " + projectId, e);
        }
    }

    /**
     * Ported from the legacy {@code getClientSideFileName}: the display name with dashes for white space, the revision
     * unless it is the head, and the format, in lower case.
     */
    private static String getClientSideFileName(ProjectDetails details,
                                                RevisionNumber revision,
                                                DownloadFormat format) {
        var revisionNumberSuffix = revision.isHead() ? "" : "-REVISION-" + revision.getValue();
        var fileName = details.getDisplayName().replaceAll("\\s+", "-")
                + revisionNumberSuffix
                + "-ontologies."
                + format.getExtension()
                + ".zip";
        return fileName.toLowerCase();
    }
}
