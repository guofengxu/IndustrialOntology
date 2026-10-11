package org.industrial.ontology.app.project;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.industrial.ontology.kernel.project.KernelThreadPools;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Owns the thread pools that every loaded project shares (docs/01 §3.4): index updates, revision writes and event
 * purges. The legacy server gave each project its own revision-writing and event-purging thread next to an
 * application-wide index pool of 10 threads; here all three pools are application-wide and a {@link ProjectContext}
 * only submits work to them.
 * <p>
 * {@link #close()} lets queued work finish and waits up to {@link #SHUTDOWN_TIMEOUT} for each pool, like the legacy
 * {@code ExecutorServiceShutdownTask}. It must run after {@link ProjectRegistry#closeAll()}: a project chains each
 * revision write onto the previous one, so a pool that stops while a project still has writes queued rejects the rest
 * of the chain and those revisions are lost. {@link ProjectRuntimeAutoConfiguration} gets this order from the bean
 * dependencies.
 */
public final class KernelExecutors implements AutoCloseable {

    /** The legacy {@code ExecutorServiceShutdownTask} waited this long for each application executor. */
    public static final Duration SHUTDOWN_TIMEOUT = Duration.ofMinutes(5);

    private static final Logger logger = LoggerFactory.getLogger(KernelExecutors.class);

    private final ExecutorService indexUpdates;

    private final ExecutorService revisionWrites;

    private final ScheduledExecutorService eventPurges;

    private final KernelThreadPools threadPools;

    /**
     * Takes ownership of the given pools: {@link #close()} shuts them down.
     */
    public KernelExecutors(@Nonnull ExecutorService indexUpdates,
                           @Nonnull ExecutorService revisionWrites,
                           @Nonnull ScheduledExecutorService eventPurges) {
        this.indexUpdates = checkNotNull(indexUpdates);
        this.revisionWrites = checkNotNull(revisionWrites);
        this.eventPurges = checkNotNull(eventPurges);
        this.threadPools = new KernelThreadPools(indexUpdates, revisionWrites, eventPurges);
    }

    /**
     * Creates fixed-size pools of non-daemon threads, so that the JVM does not exit in the middle of a revision write.
     * Index updates run in parallel within a rank of the index dependency graph, which the legacy server did on 10
     * threads. Each project keeps its revision writes in order itself, so one revision-write thread per project, as in
     * the legacy server, is not needed. Event purges are short and periodic; one thread serves all projects.
     */
    @Nonnull
    public static KernelExecutors create(int indexUpdateThreads, int revisionWriteThreads) {
        checkArgument(indexUpdateThreads > 0, "indexUpdateThreads must be positive: %s", indexUpdateThreads);
        checkArgument(revisionWriteThreads > 0, "revisionWriteThreads must be positive: %s", revisionWriteThreads);
        return new KernelExecutors(
                Executors.newFixedThreadPool(indexUpdateThreads, threadFactory("kernel-index-update-%d")),
                Executors.newFixedThreadPool(revisionWriteThreads, threadFactory("kernel-revision-write-%d")),
                Executors.newSingleThreadScheduledExecutor(threadFactory("kernel-event-purge-%d")));
    }

    private static ThreadFactory threadFactory(String nameFormat) {
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).setDaemon(false).build();
    }

    /**
     * The pools in the form that {@code ProjectContextFactory} takes them.
     */
    @Nonnull
    public KernelThreadPools threadPools() {
        return threadPools;
    }

    /**
     * Stops accepting work, lets the queued work finish and waits for it. Periodic event purges are cancelled. Calling
     * it again only waits for the pools that have not terminated yet.
     */
    @Override
    public void close() {
        var pools = List.of(new NamedPool("event purges", eventPurges),
                            new NamedPool("index updates", indexUpdates),
                            new NamedPool("revision writes", revisionWrites));
        pools.forEach(pool -> pool.executor().shutdown());
        for (var pool : pools) {
            awaitTermination(pool);
        }
    }

    private static void awaitTermination(NamedPool pool) {
        try {
            logger.info("Shutting down {} ...", pool.name());
            if (pool.executor().awaitTermination(SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                logger.info("    ... {} shut down", pool.name());
            }
            else {
                logger.error("Could not shut down {} within {}", pool.name(), SHUTDOWN_TIMEOUT);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while shutting down {}", pool.name());
        }
    }

    private record NamedPool(String name, ExecutorService executor) {
    }
}
