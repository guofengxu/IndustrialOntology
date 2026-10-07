package org.industrial.ontology.kernel.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.io.download.ProjectDownloadCacheDirectorySupplier;
import org.industrial.ontology.kernel.io.upload.DocumentResolver;
import org.industrial.ontology.kernel.io.upload.DocumentResolverImpl;
import org.industrial.ontology.kernel.lucene.ProjectLuceneDirectoryPathSupplier;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Maps {@code webprotege.data-directory} onto the legacy {@code data.directory} layout, so that a new server can
 * open a data directory written by the legacy one (docs/01 §5.4):
 * <pre>
 * &lt;data-directory&gt;/
 *   data-store/project-data/&lt;projectId&gt;/change-data/change-data.binary
 *   uploads/&lt;documentId&gt;
 *   lucene-indexes/&lt;projectId&gt;/
 *   download-cache/
 *   templates/   (overrides for the bundled e-mail templates)
 * </pre>
 * The legacy Dagger modules derived each location separately from the data directory; this class is the one place
 * that does it now, and hands out the kernel components that need a location.
 */
public final class DataDirectoryLayout {

    static final String UPLOADS_DIRECTORY_NAME = "uploads";

    static final String LUCENE_INDEXES_DIRECTORY_NAME = "lucene-indexes";

    static final String TEMPLATES_DIRECTORY_NAME = "templates";

    @Nonnull
    private final Path dataDirectory;

    public DataDirectoryLayout(@Nonnull Path dataDirectory) {
        this.dataDirectory = checkNotNull(dataDirectory).toAbsolutePath().normalize();
    }

    @Nonnull
    public Path getDataDirectory() {
        return dataDirectory;
    }

    @Nonnull
    public Path getUploadsDirectory() {
        return dataDirectory.resolve(UPLOADS_DIRECTORY_NAME);
    }

    @Nonnull
    public Path getLuceneIndexesDirectory() {
        return dataDirectory.resolve(LUCENE_INDEXES_DIRECTORY_NAME);
    }

    @Nonnull
    public Path getTemplatesDirectory() {
        return dataDirectory.resolve(TEMPLATES_DIRECTORY_NAME);
    }

    @Nonnull
    public ProjectDirectoryFactory getProjectDirectoryFactory() {
        return new ProjectDirectoryFactory(dataDirectory.toFile());
    }

    @Nonnull
    public ChangeHistoryFileFactory getChangeHistoryFileFactory() {
        return new ChangeHistoryFileFactory(getProjectDirectoryFactory());
    }

    @Nonnull
    public ProjectDownloadCacheDirectorySupplier getDownloadCacheDirectorySupplier() {
        return new ProjectDownloadCacheDirectorySupplier(dataDirectory);
    }

    @Nonnull
    public DocumentResolver getDocumentResolver() {
        return new DocumentResolverImpl(getUploadsDirectory().toFile());
    }

    @Nonnull
    public ProjectLuceneDirectoryPathSupplier getLuceneDirectoryPathSupplier(@Nonnull ProjectId projectId) {
        return new ProjectLuceneDirectoryPathSupplier(getLuceneIndexesDirectory(), projectId);
    }

    /**
     * Files such as {@code templates/watch-notification-email-template.html} are read from the data directory when
     * present and from the classpath otherwise.
     */
    @Nonnull
    public OverridableFileFactory getOverridableFileFactory() {
        return new OverridableFileFactory(dataDirectory.toFile());
    }

    @Override
    public String toString() {
        return "DataDirectoryLayout[" + dataDirectory + "]";
    }
}
