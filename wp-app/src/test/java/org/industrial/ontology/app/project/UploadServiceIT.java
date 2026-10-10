package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesDocument;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.app.ApplicationLocation;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.app.project.ProjectServiceIT.ALICE;
import static org.industrial.ontology.app.project.ProjectServiceIT.BOB;
import static org.industrial.ontology.domain.core.BuiltInRole.PROJECT_UPLOADER;

/**
 * {@link UploadService} ({@code POST /api/v1/uploads}), replacing the legacy {@code FileUploadServlet}.
 */
class UploadServiceIT {

    private static final byte[] DOCUMENT = "<rdf:RDF/>".getBytes(StandardCharsets.UTF_8);

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    private UploadService uploadService;

    private Path uploads;

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
        setMaxUploadSize(Long.MAX_VALUE);
        new ProjectTestFixture(context).grantApplicationRoles(ALICE, PROJECT_UPLOADER);
        uploadService = context.bean(UploadService.class);
        uploads = context.bean(DataDirectoryLayout.class).getUploadsDirectory();
    }

    private void setMaxUploadSize(long maxUploadSize) {
        context.bean(ApplicationPreferencesRepository.class).setApplicationPreferences(
                ApplicationPreferencesDocument.of("WebProtégé", "", new ApplicationLocation("https", "", "", 443),
                                                  maxUploadSize));
    }

    @Test
    void anUploadShouldBeStoredUnderAFreshId() throws IOException {
        var uploaded = uploadService.upload(ALICE, "pizza.owl", new ByteArrayInputStream(DOCUMENT));

        assertThat(UUID.fromString(uploaded.documentId().getDocumentId())).isNotNull();
        assertThat(uploaded.fileName()).isEqualTo("pizza.owl");
        assertThat(uploaded.size()).isEqualTo(DOCUMENT.length);
        assertThat(Files.readAllBytes(uploads.resolve(uploaded.documentId().getDocumentId()))).isEqualTo(DOCUMENT);
        assertThat(context.bean(UploadedProjectImporter.class).exists(uploaded.documentId())).isTrue();
    }

    @Test
    void uploadsShouldNeedUploadProject() throws IOException {
        var before = fileCount(uploads);

        for (var caller : new UserId[]{BOB, UserId.getGuest()}) {
            assertThatThrownBy(() -> uploadService.upload(caller, "pizza.owl", new ByteArrayInputStream(DOCUMENT)))
                    .isInstanceOf(PermissionDeniedException.class);
        }
        assertThat(fileCount(uploads)).isEqualTo(before);
    }

    @Test
    void uploadsLargerThanTheMaximumShouldBeRefusedAndRemoved() throws IOException {
        setMaxUploadSize(DOCUMENT.length);
        uploadService.upload(ALICE, "exactly.owl", new ByteArrayInputStream(DOCUMENT));
        var before = fileCount(uploads);

        setMaxUploadSize(DOCUMENT.length - 1);
        assertThatThrownBy(() -> uploadService.upload(ALICE, "pizza.owl", new ByteArrayInputStream(DOCUMENT)))
                .isInstanceOf(WpException.class)
                .extracting("code", "status").containsExactly(UploadService.UPLOAD_TOO_LARGE, 413);
        assertThat(fileCount(uploads)).isEqualTo(before);
    }

    /**
     * A zip bomb in miniature: the archive is small, its contents are not.
     */
    @Test
    void zipArchivesWhoseContentsExceedTheMaximumShouldBeRefused() throws IOException {
        var archive = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(archive)) {
            zip.putNextEntry(new ZipEntry("root-ontology.owl"));
            zip.write(new byte[100_000]);
            zip.closeEntry();
        }
        setMaxUploadSize(10_000);
        assertThat(archive.size()).isLessThan(10_000);
        var before = fileCount(uploads);

        assertThatThrownBy(() -> uploadService.upload(ALICE, "pizza.zip",
                                                      new ByteArrayInputStream(archive.toByteArray())))
                .isInstanceOf(WpException.class)
                .extracting("code").isEqualTo(UploadService.UPLOAD_TOO_LARGE);
        assertThat(fileCount(uploads)).isEqualTo(before);
    }

    /**
     * The first local header claims a data descriptor for a stored entry: {@code ZipInputStream} gives up there, but
     * {@code ZipFile}, which extracts the archive when the project is created, reads the central directory and does
     * not look at that flag. The contents behind that entry must still be counted.
     */
    @Test
    void contentsBehindAnEntryThatOnlyTheStreamingReaderRefusesShouldBeCounted() throws IOException {
        var archive = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(archive)) {
            var stored = new ZipEntry("readme.txt");
            stored.setMethod(ZipEntry.STORED);
            stored.setSize(DOCUMENT.length);
            stored.setCompressedSize(DOCUMENT.length);
            var crc = new CRC32();
            crc.update(DOCUMENT);
            stored.setCrc(crc.getValue());
            zip.putNextEntry(stored);
            zip.write(DOCUMENT);
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("root-ontology.owl"));
            zip.write(new byte[100_000]);
            zip.closeEntry();
        }
        var bytes = archive.toByteArray();
        // Bit 3 of the general purpose flag of the first local header: "sizes follow in a data descriptor"
        bytes[6] |= 0x08;
        setMaxUploadSize(10_000);
        assertThat(bytes.length).isLessThan(10_000);
        var before = fileCount(uploads);

        assertThatThrownBy(() -> uploadService.upload(ALICE, "pizza.zip", new ByteArrayInputStream(bytes)))
                .isInstanceOf(WpException.class)
                .extracting("code", "status").containsExactly(UploadService.UPLOAD_TOO_LARGE, 413);
        assertThat(fileCount(uploads)).isEqualTo(before);
    }

    @Test
    void zipArchivesThatCannotBeReadShouldBeRefused() throws IOException {
        var archive = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(archive)) {
            zip.putNextEntry(new ZipEntry("root-ontology.owl"));
            zip.write(DOCUMENT);
            zip.closeEntry();
        }
        // Without its end record and part of its central directory, but the first entry is still intact
        var truncated = Arrays.copyOf(archive.toByteArray(), archive.size() - 30);
        var before = fileCount(uploads);

        assertThatThrownBy(() -> uploadService.upload(ALICE, "pizza.zip", new ByteArrayInputStream(truncated)))
                .isInstanceOf(WpException.class)
                .extracting("code", "status").containsExactly(ProjectService.INVALID_UPLOAD, 400);
        assertThat(fileCount(uploads)).isEqualTo(before);
    }

    /**
     * The files in the uploads directory, which the tests of this class share.
     */
    private static long fileCount(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return 0;
        }
        try (var files = Files.list(directory)) {
            return files.count();
        }
    }
}
