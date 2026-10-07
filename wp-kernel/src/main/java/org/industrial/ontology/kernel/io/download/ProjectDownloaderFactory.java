package org.industrial.ontology.kernel.io.download;

import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ProjectDownloader}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.download.ProjectDownloaderFactory} (generated in the legacy build).
 */
public final class ProjectDownloaderFactory {

    private final Supplier<PrefixDeclarationsStore> prefixDeclarationsStore;

    public ProjectDownloaderFactory(Supplier<PrefixDeclarationsStore> prefixDeclarationsStore) {
        this.prefixDeclarationsStore = java.util.Objects.requireNonNull(prefixDeclarationsStore);
    }

    public ProjectDownloader create(@Nonnull ProjectId projectId, @Nonnull String fileName, @Nonnull RevisionNumber revision, @Nonnull DownloadFormat format, @Nonnull RevisionManager revisionManager) {
        return new ProjectDownloader(projectId, fileName, revision, format, revisionManager, prefixDeclarationsStore.get());
    }
}
