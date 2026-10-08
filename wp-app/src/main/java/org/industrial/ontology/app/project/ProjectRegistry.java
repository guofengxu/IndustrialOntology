package org.industrial.ontology.app.project;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.project.ProjectContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * The projects loaded in this process (docs/01 §2), replacing the legacy {@code ProjectCache} and its
 * {@code ProjectCacheManager}. Application services obtain a project's {@link ProjectContext} here and never build
 * one themselves.
 * <p>
 * A project is loaded on its first {@link #get} and closed once it has not been used for the dormant time
 * ({@code webprotege.project.dormant-time}, legacy {@code project.dormant.time}). Caffeine's
 * {@code expireAfterAccess} tells when a project has become dormant. The legacy cache checked every 30 seconds; here
 * the registry's own thread is woken when the next project falls dormant and closes it there.
 * <p>
 * The registry, not Caffeine, tracks which context is open, because Caffeine reports removals asynchronously. A
 * context is opened and closed only while its project's lock is held, and a project is never opened again before its
 * previous context is closed: the new context could not obtain the Lucene write lock. Loading one project does not
 * block the others.
 * <p>
 * A context stays open while it is used, so callers get it for one request and do not keep it. Closing a context
 * waits for its read/write lock, so a caller must not call {@link #get} or {@link #close} while it holds a project's
 * lock. The registry opens any project id it is given (the kernel creates an empty project for an unknown id), so
 * callers check that the project exists first.
 */
public class ProjectRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRegistry.class);

    private static final Duration MAINTENANCE_SHUTDOWN_TIMEOUT = KernelExecutors.SHUTDOWN_TIMEOUT;

    private final Function<ProjectId, ProjectContext> loader;

    private final Duration dormantTime;

    /** Projects by last use. An entry expires when its project becomes dormant, and its context is then closed. */
    private final Cache<ProjectId, ProjectContext> recentlyUsed;

    /** The open context of each loaded project, changed only while the project's lock is held. */
    private final ConcurrentMap<ProjectId, ProjectContext> openContexts = new ConcurrentHashMap<>();

    /** Gives equal project ids one lock object while they are in use, as the legacy {@code ProjectCache} did. */
    private final Interner<ProjectId> projectLocks = Interners.newWeakInterner();

    /** Loads hold the read lock and {@link #closeAll()} takes the write lock, so that no load outlives closeAll. */
    private final ReadWriteLock registryLock = new ReentrantReadWriteLock();

    /** Guarded by {@link #registryLock}. */
    private boolean closed;

    @Nullable
    private final ExecutorService ownedExecutor;

    /**
     * @param dormantTime how long a project stays loaded after its last use
     */
    public ProjectRegistry(@Nonnull ProjectContextFactory factory, @Nonnull Duration dormantTime) {
        this(checkNotNull(factory)::create, dormantTime, Ticker.systemTicker(), newMaintenanceExecutor());
    }

    private ProjectRegistry(Function<ProjectId, ProjectContext> loader,
                            Duration dormantTime,
                            Ticker ticker,
                            ScheduledExecutorService maintenance) {
        this(loader, dormantTime, ticker, Scheduler.forScheduledExecutorService(maintenance), maintenance, maintenance);
    }

    /**
     * @param scheduler     wakes the registry when the next project becomes dormant
     * @param executor      runs the removal notifications, which close dormant projects
     * @param ownedExecutor shut down by {@link #closeAll()}, or {@code null} if the caller owns the executor
     */
    ProjectRegistry(@Nonnull Function<ProjectId, ProjectContext> loader,
                    @Nonnull Duration dormantTime,
                    @Nonnull Ticker ticker,
                    @Nonnull Scheduler scheduler,
                    @Nonnull Executor executor,
                    @Nullable ExecutorService ownedExecutor) {
        this.loader = checkNotNull(loader);
        this.dormantTime = checkNotNull(dormantTime);
        checkArgument(dormantTime.compareTo(Duration.ZERO) > 0, "dormantTime must be positive: %s", dormantTime);
        this.ownedExecutor = ownedExecutor;
        this.recentlyUsed = Caffeine.newBuilder()
                                    .expireAfterAccess(dormantTime)
                                    .ticker(checkNotNull(ticker))
                                    .scheduler(checkNotNull(scheduler))
                                    .executor(checkNotNull(executor))
                                    .<ProjectId, ProjectContext>removalListener(this::closeIfStillOpen)
                                    .build();
        logger.info("Dormant project time: {}", dormantTime);
    }

    private static ScheduledExecutorService newMaintenanceExecutor() {
        // Daemon: closing the projects on shutdown is closeAll's job, not this thread's.
        var executor = new ScheduledThreadPoolExecutor(
                1, new ThreadFactoryBuilder().setNameFormat("project-registry-%d").setDaemon(true).build());
        // On closeAll, a dormant project being closed is waited for, but the next dormancy check is dropped
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    @Nonnull
    public Duration dormantTime() {
        return dormantTime;
    }

    /**
     * Returns the project's open context, loading the project if needed, and records the use. Concurrent calls for a
     * project that is not loaded load it once; the other callers wait and get the same context.
     *
     * @throws IllegalStateException if {@link #closeAll()} has been called
     */
    @Nonnull
    public ProjectContext get(@Nonnull ProjectId projectId) {
        checkNotNull(projectId);
        var context = recentlyUsed.getIfPresent(projectId);
        if (context == null || context.isClosed()) {
            context = load(projectId);
        }
        context.touch(Instant.now());
        return context;
    }

    /**
     * Returns the project's context if the project is loaded, without loading it or counting as a use, so that
     * polling a project does not keep it from becoming dormant. The legacy {@code getProjectEventManagerIfActive}
     * read project events this way.
     */
    @Nonnull
    public Optional<ProjectContext> getIfLoaded(@Nonnull ProjectId projectId) {
        checkNotNull(projectId);
        return Optional.ofNullable(recentlyUsed.policy().getIfPresentQuietly(projectId))
                       .filter(context -> !context.isClosed());
    }

    /**
     * The projects that have an open context.
     */
    @Nonnull
    public Set<ProjectId> loadedProjects() {
        return Set.copyOf(openContexts.keySet());
    }

    /**
     * Closes the project if it is loaded; the next {@link #get} loads it again.
     */
    public void close(@Nonnull ProjectId projectId) {
        close(checkNotNull(projectId), "on request");
    }

    /**
     * Closes every loaded project and refuses to load any afterwards; called on shutdown (docs/01 §7). Loads in
     * progress finish first. Closing a context waits for its pending revisions to be written, so when this returns
     * the {@link KernelExecutors} can be shut down without losing revisions.
     */
    public void closeAll() {
        registryLock.writeLock().lock();
        try {
            if (closed) {
                return;
            }
            closed = true;
        }
        finally {
            registryLock.writeLock().unlock();
        }
        logger.info("Closing {} loaded projects", openContexts.size());
        for (var projectId : List.copyOf(openContexts.keySet())) {
            close(projectId, "shutdown");
        }
        recentlyUsed.cleanUp();
        if (ownedExecutor != null) {
            // The registry thread may be closing a dormant project that was no longer listed above; wait for it
            shutDown(ownedExecutor);
        }
        logger.info("Closed all projects");
    }

    /**
     * Runs Caffeine's pending maintenance now, which closes the projects that have become dormant.
     */
    void cleanUp() {
        recentlyUsed.cleanUp();
    }

    private ProjectContext load(ProjectId projectId) {
        registryLock.readLock().lock();
        try {
            checkState(!closed, "The project registry has been closed");
            synchronized (lockFor(projectId)) {
                var context = recentlyUsed.getIfPresent(projectId);
                if (context != null && !context.isClosed()) {
                    // Another thread loaded the project while this one waited for the lock
                    return context;
                }
                // The project has become dormant but its context has not been closed yet
                closeOpenContext(projectId, "dormant");
                context = loader.apply(projectId);
                openContexts.put(projectId, context);
                recentlyUsed.put(projectId, context);
                logger.info("{} Loaded project.  {} projects are now loaded.", projectId, openContexts.size());
                return context;
            }
        }
        finally {
            registryLock.readLock().unlock();
        }
    }

    private void close(ProjectId projectId, String reason) {
        synchronized (lockFor(projectId)) {
            recentlyUsed.invalidate(projectId);
            closeOpenContext(projectId, reason);
        }
    }

    /**
     * Caffeine's removal listener. Contexts that were closed on request or replaced by a reload are no longer open and
     * are left alone, so in effect this closes the projects that have become dormant.
     */
    private void closeIfStillOpen(@Nullable ProjectId projectId,
                                  @Nullable ProjectContext context,
                                  @Nonnull RemovalCause cause) {
        if (projectId == null || context == null) {
            return;
        }
        synchronized (lockFor(projectId)) {
            if (openContexts.remove(projectId, context)) {
                closeContext(projectId, context, cause == RemovalCause.EXPIRED ? "dormant" : cause.name());
            }
        }
    }

    /** Requires the project's lock. */
    private void closeOpenContext(ProjectId projectId, String reason) {
        var context = openContexts.remove(projectId);
        if (context != null) {
            closeContext(projectId, context, reason);
        }
    }

    private void closeContext(ProjectId projectId, ProjectContext context, String reason) {
        context.close();
        logger.info("{} Closed project ({}).  {} projects are now loaded.", projectId, reason, openContexts.size());
    }

    private Object lockFor(ProjectId projectId) {
        return projectLocks.intern(projectId);
    }

    private static void shutDown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(MAINTENANCE_SHUTDOWN_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
                logger.error("The project registry thread did not stop within {}", MAINTENANCE_SHUTDOWN_TIMEOUT);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while stopping the project registry thread");
        }
    }
}
