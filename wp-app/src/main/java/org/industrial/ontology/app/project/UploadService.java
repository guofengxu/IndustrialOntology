package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.util.ZipInputStreamChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.UPLOAD_PROJECT;

/**
 * Stores uploaded ontology documents for project creation (docs/02 §3 {@code POST /api/v1/uploads}), replacing the
 * legacy {@code FileUploadServlet}: the caller needs {@code UploadProject}, the file goes to the uploads directory
 * under a fresh id, and {@link ProjectService#createProject} imports it by that id.
 * <p>
 * The size limit is the application settings' {@code maxUploadSize}; for a zip archive it applies to the expanded
 * contents too, as with the legacy {@code FileContentsSizeCalculator}. The contents are counted while they are
 * expanded rather than read from the archive's headers, which a crafted archive could understate, and with the reader
 * that later extracts them. A zip archive that cannot be read is refused at once ({@code INVALID_UPLOAD}, 400).
 */
public class UploadService {

    /**
     * The upload, or the expanded contents of the zip archive, exceeds the maximum upload size.
     */
    public static final String UPLOAD_TOO_LARGE = "UPLOAD_TOO_LARGE";

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private static final int BUFFER_SIZE = 64 * 1024;

    private final AccessManager accessManager;

    private final ApplicationPreferencesRepository applicationPreferencesRepository;

    private final DataDirectoryLayout dataDirectoryLayout;

    public UploadService(@Nonnull AccessManager accessManager,
                         @Nonnull ApplicationPreferencesRepository applicationPreferencesRepository,
                         @Nonnull DataDirectoryLayout dataDirectoryLayout) {
        this.accessManager = checkNotNull(accessManager);
        this.applicationPreferencesRepository = checkNotNull(applicationPreferencesRepository);
        this.dataDirectoryLayout = checkNotNull(dataDirectoryLayout);
    }

    /**
     * Stores the uploaded file and returns its id.
     *
     * @param fileName the name of the file on the client, only reported back
     * @throws PermissionDeniedException if the caller may not upload projects
     * @throws WpException               {@value #UPLOAD_TOO_LARGE} (413) if the file or its expanded contents are
     *                                   larger than the maximum upload size; {@code INVALID_UPLOAD} (400) for a zip
     *                                   archive that cannot be read
     */
    @Nonnull
    public UploadedDocument upload(@Nonnull UserId caller, @Nullable String fileName, @Nonnull InputStream content) {
        accessManager.requireSignedIn(caller);
        accessManager.require(caller, ApplicationResource.get(), UPLOAD_PROJECT);
        var maxUploadSize = applicationPreferencesRepository.getApplicationPreferences().maxUploadSize();
        var documentId = new DocumentId(UUID.randomUUID().toString());
        var file = dataDirectoryLayout.getDocumentResolver().resolve(documentId);
        try {
            Files.createDirectories(file.getParent());
            long size;
            try (var out = Files.newOutputStream(file)) {
                size = copyAtMost(content, out, maxUploadSize);
            }
            boolean tooLarge;
            try {
                tooLarge = size > maxUploadSize || expandedSizeExceeds(file, maxUploadSize);
            } catch (ZipException e) {
                Files.deleteIfExists(file);
                logger.info("Refused upload from {}: the zip archive cannot be read: {}", caller, e.getMessage());
                throw new WpException(ProjectService.INVALID_UPLOAD, 400, "The zip archive cannot be read");
            }
            if (tooLarge) {
                Files.deleteIfExists(file);
                throw new WpException(UPLOAD_TOO_LARGE, 413, String.format(
                        "The file is too large: files, or the contents of zip archives, must not exceed %d MB",
                        maxUploadSize / (1024 * 1024)));
            }
            logger.info("Stored upload {} ({} bytes) from {}", documentId.getDocumentId(), size, caller);
            return new UploadedDocument(documentId, fileName == null ? "" : fileName, size);
        } catch (IOException e) {
            deleteQuietly(file);
            throw new UncheckedIOException("Could not store the uploaded file", e);
        }
    }

    /**
     * Copies until {@code limit + 1} bytes have been copied, so that a too large upload is not written in full.
     */
    private static long copyAtMost(InputStream in, OutputStream out, long limit) throws IOException {
        var buffer = new byte[BUFFER_SIZE];
        long total = 0;
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
            total += read;
            if (total > limit) {
                break;
            }
        }
        return total;
    }

    /**
     * Whether the expanded contents of a zip archive exceed {@code limit}. The entries are read with {@link ZipFile},
     * as {@code ZipFileExtractor} reads them when the project is created, and counted as they are inflated, so the
     * count is what extraction will write. A streaming reader would see a different archive: it stops at the first
     * local header it does not like, and never sees entries that only the central directory lists.
     *
     * @throws ZipException if the archive cannot be read
     */
    private static boolean expandedSizeExceeds(Path file, long limit) throws IOException {
        if (!new ZipInputStreamChecker().isZipFile(file.toFile())) {
            return false;
        }
        var buffer = new byte[BUFFER_SIZE];
        long total = 0;
        try (var zip = new ZipFile(file.toFile())) {
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                try (var entry = zip.getInputStream(entries.nextElement())) {
                    int read;
                    while ((read = entry.read(buffer)) != -1) {
                        total += read;
                        if (total > limit) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            logger.warn("Could not delete the partial upload {}: {}", file, e.getMessage());
        }
    }
}
