package org.industrial.ontology.kernel.project;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The thread pools that every project shares (docs/01 §3.4). wp-app's {@code KernelExecutors} bean owns them and
 * shuts them down; a {@link ProjectContext} only submits work and never shuts a pool down.
 *
 * @param indexUpdates  builds and updates the primary indexes in parallel, rank by rank
 * @param revisionWrites appends revisions to {@code change-data.binary}
 * @param eventPurges    purges expired project events
 */
public record KernelThreadPools(@Nonnull ExecutorService indexUpdates,
                                @Nonnull ExecutorService revisionWrites,
                                @Nonnull ScheduledExecutorService eventPurges) {

    public KernelThreadPools {
        Objects.requireNonNull(indexUpdates, "indexUpdates");
        Objects.requireNonNull(revisionWrites, "revisionWrites");
        Objects.requireNonNull(eventPurges, "eventPurges");
    }
}
