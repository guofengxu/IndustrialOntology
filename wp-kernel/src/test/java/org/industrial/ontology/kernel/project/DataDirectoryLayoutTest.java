package org.industrial.ontology.kernel.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.upload.DocumentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Pins the legacy {@code data.directory} layout (docs/01 §5.4); a change here breaks reading data directories
 * written by the legacy server.
 */
public class DataDirectoryLayoutTest {

    private static final ProjectId PROJECT_ID = ProjectId.get("12345678-1234-1234-1234-123456789abc");

    @TempDir
    Path root;

    private DataDirectoryLayout layout;

    @BeforeEach
    public void setUp() {
        layout = new DataDirectoryLayout(root);
    }

    @Test
    public void shouldPlaceChangeHistoryUnderDataStore() {
        var expected = root.resolve("data-store/project-data/12345678-1234-1234-1234-123456789abc/change-data/change-data.binary");
        assertThat(layout.getChangeHistoryFileFactory().getChangeHistoryFile(PROJECT_ID).toPath(), is(expected));
    }

    @Test
    public void shouldPlaceProjectDirectoryUnderDataStore() {
        var expected = root.resolve("data-store/project-data/12345678-1234-1234-1234-123456789abc");
        assertThat(layout.getProjectDirectoryFactory().getProjectDirectory(PROJECT_ID).toPath(), is(expected));
    }

    @Test
    public void shouldResolveUploadsInUploadsDirectory() {
        assertThat(layout.getDocumentResolver().resolve(new DocumentId("upload-1")),
                   is(root.resolve("uploads/upload-1")));
    }

    @Test
    public void shouldPlaceLuceneIndexesPerProject() {
        assertThat(layout.getLuceneDirectoryPathSupplier(PROJECT_ID).get(),
                   is(root.resolve("lucene-indexes/12345678-1234-1234-1234-123456789abc")));
    }

    @Test
    public void shouldPlaceDownloadCacheInDataDirectory() {
        assertThat(layout.getDownloadCacheDirectorySupplier().get(), is(root.resolve("download-cache")));
    }

    @Test
    public void shouldReadTemplateOverridesFromTemplatesDirectory() throws IOException {
        var template = layout.getTemplatesDirectory().resolve("watch-notification-email-template.html");
        Files.createDirectories(template.getParent());
        Files.writeString(template, "<p>override</p>");
        var file = layout.getOverridableFileFactory()
                         .getOverridableFile("templates/watch-notification-email-template.html")
                         .get();
        assertThat(file.toPath(), is(template));
    }

    @Test
    public void shouldNormaliseRelativeDataDirectory() {
        var relative = new DataDirectoryLayout(Path.of("data", "..", "data"));
        assertThat(relative.getDataDirectory(), is(Path.of("data").toAbsolutePath()));
    }
}
