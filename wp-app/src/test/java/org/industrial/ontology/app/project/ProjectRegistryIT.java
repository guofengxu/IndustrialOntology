package org.industrial.ontology.app.project;

import com.github.benmanes.caffeine.cache.Scheduler;
import com.github.benmanes.caffeine.cache.Ticker;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.project.ProjectRuntimeTestSupport.GatedThreadPool;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.project.ProjectContext;
import org.industrial.ontology.kernel.project.ProjectContextFactory;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.industrial.ontology.app.project.ProjectRuntimeTestSupport.contextFactory;
import static org.industrial.ontology.app.project.ProjectRuntimeTestSupport.savedRevisionCount;
import static org.industrial.ontology.app.project.ProjectRuntimeTestSupport.withGatedRevisionWrites;
import static org.industrial.ontology.kernel.project.ProjectKernelFixture.createClass;

/**
 * {@link ProjectRegistry} over the real kernel (docs/01 §2, §7; docs/07 S3): a project is closed once it has been
 * dormant for the dormant time and comes back with what it wrote; concurrent requests load a project once; a dormant
 * project is closed before it is opened again; {@link ProjectRegistry#closeAll()} waits for pending revisions.
 */
class ProjectRegistryIT {

    private static final Duration DORMANT_TIME = Duration.ofSeconds(1);

    /** Long enough that no project becomes dormant during a test that is not about dormancy. */
    private static final Duration LONG_DORMANT_TIME = Duration.ofMinutes(10);

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    @TempDir
    Path dataDirectory;

    private final List<ProjectRegistry> registries = new ArrayList<>();

    private final ProjectId projectId = ProjectKernelFixture.freshProjectId();

    private KernelExecutors executors;

    private ProjectContextFactory factory;

    @BeforeEach
    void setUp() {
        executors = KernelExecutors.create(4, 2);
        factory = contextFactory(dataDirectory, executors);
    }

    @AfterEach
    void tearDown() {
        registries.forEach(ProjectRegistry::closeAll);
        executors.close();
    }

    private ProjectRegistry registry(Duration dormantTime) {
        return register(new ProjectRegistry(factory, dormantTime));
    }

    /**
     * A registry whose clock, maintenance and removal notifications the test controls.
     */
    private ProjectRegistry registry(Function<ProjectId, ProjectContext> loader, Ticker ticker, Executor executor) {
        return register(new ProjectRegistry(loader,
                                            DORMANT_TIME,
                                            ticker,
                                            Scheduler.disabledScheduler(),
                                            executor,
                                            null));
    }

    private ProjectRegistry register(ProjectRegistry registry) {
        registries.add(registry);
        return registry;
    }

    @Test
    void shouldCloseProjectOnceItHasBeenDormantForTheDormantTime() {
        var registry = registry(DORMANT_TIME);
        var start = System.nanoTime();
        var context = registry.get(projectId);
        assertThat(registry.loadedProjects()).containsExactly(projectId);

        await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(10)).until(context::isClosed);

        assertThat(elapsedSince(start)).isGreaterThanOrEqualTo(DORMANT_TIME);
        assertThat(registry.loadedProjects()).isEmpty();
        assertThat(registry.getIfLoaded(projectId)).isEmpty();
    }

    @Test
    void shouldKeepProjectLoadedWhileItIsUsed() {
        var dormantTime = Duration.ofSeconds(2);
        var registry = registry(dormantTime);
        var context = registry.get(projectId);
        var firstUse = context.lastAccess();

        var end = System.nanoTime() + dormantTime.multipliedBy(2).toNanos();
        while (System.nanoTime() < end) {
            sleep(Duration.ofMillis(200));
            assertThat(registry.get(projectId)).isSameAs(context);
        }

        assertThat(context.isClosed()).isFalse();
        assertThat(context.lastAccess()).isAfter(firstUse);
        await().atMost(TIMEOUT).until(context::isClosed);
    }

    @Test
    void quietLookupsShouldNeitherLoadProjectNorKeepItLoaded() {
        var registry = registry(DORMANT_TIME);
        assertThat(registry.getIfLoaded(projectId)).isEmpty();
        assertThat(registry.loadedProjects()).isEmpty();

        var start = System.nanoTime();
        var context = registry.get(projectId);
        var lastUse = context.lastAccess();
        assertThat(registry.getIfLoaded(projectId)).containsSame(context);

        await().atMost(TIMEOUT).pollInterval(Duration.ofMillis(50)).until(() -> {
            registry.getIfLoaded(projectId);
            return context.isClosed();
        });

        assertThat(elapsedSince(start)).isGreaterThanOrEqualTo(DORMANT_TIME);
        assertThat(context.lastAccess()).isEqualTo(lastUse);
    }

    /**
     * The test decides when the project becomes dormant: with the real clock and a one-second dormant time, a busy
     * machine closed the project while the classes were still being written (the writes go to the context, not
     * through the registry, so they do not count as use). That closing on time is
     * {@link #shouldCloseProjectOnceItHasBeenDormantForTheDormantTime}.
     */
    @Test
    void shouldReloadDormantProjectWithWhatItWroteBeforeItWasClosed() {
        var ticker = new FakeTicker();
        Queue<Runnable> deferred = new ConcurrentLinkedQueue<>();
        var registry = registry(factory::create, ticker, deferred::add);
        var context = registry.get(projectId);
        var pizza = createClass(context, "Pizza", ImmutableSet.of());
        var margherita = createClass(context, "Margherita", ImmutableSet.of(pizza));
        ticker.advance(DORMANT_TIME.multipliedBy(2));

        var reloaded = registry.get(projectId);
        runAll(deferred);

        assertThat(context.isClosed()).isTrue();
        assertThat(reloaded).isNotSameAs(context);
        assertThat(reloaded.isClosed()).isFalse();
        assertThat(reloaded.revisionManager().getRevisions()).hasSize(2);
        assertThat(reloaded.hierarchies().classHierarchy().getChildren(pizza)).containsExactly(margherita);
        assertThat(ProjectKernelFixture.searchClasses(reloaded, "marg")).containsExactly(margherita);
    }

    /**
     * Caffeine reports an expired entry only when its maintenance runs. A request that arrives in between must not
     * open a second context next to the dormant one: that context could not obtain the Lucene write lock.
     */
    @Test
    void shouldCloseDormantContextBeforeOpeningProjectAgainWhenEvictionHasNotRunYet() {
        var ticker = new FakeTicker();
        Queue<Runnable> deferred = new ConcurrentLinkedQueue<>();
        var registry = registry(factory::create, ticker, deferred::add);
        var dormant = registry.get(projectId);

        ticker.advance(DORMANT_TIME.multipliedBy(2));
        var reopened = registry.get(projectId);

        assertThat(reopened).isNotSameAs(dormant);
        assertThat(dormant.isClosed()).isTrue();
        assertThat(reopened.isClosed()).isFalse();

        // The late removal notification for the dormant context leaves the new one open
        runAll(deferred);
        registry.cleanUp();
        runAll(deferred);
        assertThat(reopened.isClosed()).isFalse();
        assertThat(registry.get(projectId)).isSameAs(reopened);
        assertThat(registry.loadedProjects()).containsExactly(projectId);
    }

    @Test
    void shouldLoadProjectOnceWhenItIsRequestedConcurrently() throws Exception {
        var loads = new AtomicInteger();
        // A clock that stands still: with the real one, a slow load outlasted the one-second dormant time.
        var registry = registry(id -> {
            loads.incrementAndGet();
            // Keeps the other requests waiting while the project loads
            sleep(Duration.ofMillis(300));
            return factory.create(id);
        }, new FakeTicker(), Runnable::run);
        var requests = 16;
        var start = new CountDownLatch(1);
        var pool = Executors.newFixedThreadPool(requests);
        try {
            var futures = new ArrayList<Future<ProjectContext>>();
            for (var i = 0; i < requests; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    return registry.get(projectId);
                }));
            }
            start.countDown();

            Set<ProjectContext> contexts = Collections.newSetFromMap(new IdentityHashMap<>());
            for (var future : futures) {
                contexts.add(future.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS));
            }
            assertThat(contexts).hasSize(1);
            assertThat(loads).hasValue(1);
        }
        finally {
            pool.shutdownNow();
        }
    }

    @Test
    void shouldLoadDifferentProjectsAtTheSameTime() throws Exception {
        var bothLoading = new CyclicBarrier(2);
        // A clock that stands still: with the real one, the first project went dormant on a slow run.
        var registry = registry(id -> {
            // Passes only if the other project is being loaded at the same time
            awaitBarrier(bothLoading);
            return factory.create(id);
        }, new FakeTicker(), Runnable::run);
        var otherProjectId = ProjectKernelFixture.freshProjectId();
        var pool = Executors.newFixedThreadPool(2);
        try {
            var first = pool.submit(() -> registry.get(projectId));
            var second = pool.submit(() -> registry.get(otherProjectId));

            assertThat(first.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS).projectId()).isEqualTo(projectId);
            assertThat(second.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS).projectId()).isEqualTo(otherProjectId);
            assertThat(registry.loadedProjects()).containsExactlyInAnyOrder(projectId, otherProjectId);
        }
        finally {
            pool.shutdownNow();
        }
    }

    @Test
    void shouldCloseProjectOnRequestAndLoadItAgainOnItsNextUse() {
        var registry = registry(LONG_DORMANT_TIME);
        var context = registry.get(projectId);
        createClass(context, "Pizza", ImmutableSet.of());

        registry.close(projectId);

        assertThat(context.isClosed()).isTrue();
        assertThat(registry.loadedProjects()).isEmpty();
        assertThat(registry.getIfLoaded(projectId)).isEmpty();
        var reloaded = registry.get(projectId);
        assertThat(reloaded).isNotSameAs(context);
        assertThat(reloaded.revisionManager().getRevisions()).hasSize(1);
    }

    @Test
    void closeAllShouldCloseEveryLoadedProjectAndRefuseToLoadMore() {
        var registry = registry(LONG_DORMANT_TIME);
        var first = registry.get(projectId);
        var second = registry.get(ProjectKernelFixture.freshProjectId());

        // The next dormancy check is 10 minutes away; closeAll must not wait for it
        assertTimeout(Duration.ofSeconds(20), registry::closeAll);
        registry.closeAll();

        assertThat(first.isClosed()).isTrue();
        assertThat(second.isClosed()).isTrue();
        assertThat(registry.loadedProjects()).isEmpty();
        assertThatIllegalStateException().isThrownBy(() -> registry.get(projectId));
    }

    /**
     * Graceful shutdown (docs/01 §7): when closeAll returns, revisions that were still waiting for the revision-write
     * pool are on disk, so the pools can be stopped at once.
     */
    @Test
    void closeAllShouldWaitUntilPendingRevisionsAreWritten() throws Exception {
        var revisionWrites = new GatedThreadPool();
        var gatedExecutors = withGatedRevisionWrites(revisionWrites);
        try {
            var registry = new ProjectRegistry(contextFactory(dataDirectory, gatedExecutors), LONG_DORMANT_TIME);
            var context = registry.get(projectId);
            for (var i = 1; i <= 5; i++) {
                createClass(context, "Class" + i, ImmutableSet.of());
            }
            // The first revision is saved at once; the other four wait for the pool
            assertThat(revisionWrites.getCompletedTaskCount()).isZero();

            var closing = CompletableFuture.runAsync(registry::closeAll);
            assertThatThrownBy(() -> closing.get(500, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            revisionWrites.open();
            closing.get(TIMEOUT.toSeconds(), TimeUnit.SECONDS);

            assertThat(context.isClosed()).isTrue();
        }
        finally {
            // Nothing that is still queued survives this
            revisionWrites.shutdownNow();
            gatedExecutors.close();
        }
        assertThat(savedRevisionCount(dataDirectory, projectId)).isEqualTo(5);
    }

    private static Duration elapsedSince(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }

    private static void runAll(Queue<Runnable> tasks) {
        Runnable task;
        while ((task = tasks.poll()) != null) {
            task.run();
        }
    }

    private static void awaitBarrier(CyclicBarrier barrier) {
        try {
            barrier.await(10, TimeUnit.SECONDS);
        }
        catch (Exception e) {
            throw new IllegalStateException("The other project was not loaded at the same time", e);
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static final class FakeTicker implements Ticker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }
}
