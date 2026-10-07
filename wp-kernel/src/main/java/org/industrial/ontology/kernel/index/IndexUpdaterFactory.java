package org.industrial.ontology.kernel.index;

import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.industrial.ontology.domain.core.ProjectId;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link IndexUpdater}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.index.impl.IndexUpdaterFactory} (generated in the legacy build).
 */
public final class IndexUpdaterFactory {

    private final Supplier<RevisionManager> revisionManager;

    private final Supplier<Set<UpdatableIndex>> indexes;

    private final Supplier<ExecutorService> indexUpdaterService;

    private final Supplier<ProjectId> projectId;

    public IndexUpdaterFactory(Supplier<RevisionManager> revisionManager,
            Supplier<Set<UpdatableIndex>> indexes,
            Supplier<ExecutorService> indexUpdaterService,
            Supplier<ProjectId> projectId) {
        this.revisionManager = java.util.Objects.requireNonNull(revisionManager);
        this.indexes = java.util.Objects.requireNonNull(indexes);
        this.indexUpdaterService = java.util.Objects.requireNonNull(indexUpdaterService);
        this.projectId = java.util.Objects.requireNonNull(projectId);
    }

    public IndexUpdater create() {
        return new IndexUpdater(revisionManager.get(), indexes.get(), indexUpdaterService.get(), projectId.get());
    }
}
