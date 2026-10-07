package org.industrial.ontology.kernel.io.download;




import java.nio.file.Path;
import java.util.function.Supplier;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.download.ProjectDownloadCacheDirectorySupplier}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 14 Apr 2017
 */
public class ProjectDownloadCacheDirectorySupplier implements Supplier<Path> {

    private static final String DIRECTORY_NAME = "download-cache";

    private final Path dataDirectory;

    /** @param dataDirectory {@code webprotege.data-directory} (docs/01 Â§5.4) */
    public ProjectDownloadCacheDirectorySupplier(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    @Override
    public Path get() {
        return dataDirectory.resolve(DIRECTORY_NAME);
    }
}
