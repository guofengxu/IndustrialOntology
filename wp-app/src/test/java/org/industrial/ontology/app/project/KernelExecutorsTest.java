package org.industrial.ontology.app.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * {@link KernelExecutors} provides the pools that all projects share (docs/01 §3.4) and, on close, lets the queued
 * work finish.
 */
class KernelExecutorsTest {

    private KernelExecutors executors;

    @AfterEach
    void tearDown() {
        if (executors != null) {
            executors.close();
        }
    }

    @Test
    void shouldCreateFixedPoolsOfTheConfiguredSizes() {
        executors = KernelExecutors.create(3, 2);

        var pools = executors.threadPools();
        assertThat(((ThreadPoolExecutor) pools.indexUpdates()).getCorePoolSize()).isEqualTo(3);
        assertThat(((ThreadPoolExecutor) pools.indexUpdates()).getMaximumPoolSize()).isEqualTo(3);
        assertThat(((ThreadPoolExecutor) pools.revisionWrites()).getCorePoolSize()).isEqualTo(2);
    }

    @Test
    void shouldRunWorkOnNamedNonDaemonThreads() throws Exception {
        executors = KernelExecutors.create(1, 1);
        var pools = executors.threadPools();

        assertThat(threadOf(pools.indexUpdates())).startsWith("kernel-index-update-").endsWith("daemon=false");
        assertThat(threadOf(pools.revisionWrites())).startsWith("kernel-revision-write-").endsWith("daemon=false");
        assertThat(threadOf(pools.eventPurges())).startsWith("kernel-event-purge-").endsWith("daemon=false");
    }

    private static String threadOf(ExecutorService executor) throws Exception {
        return executor.submit(() -> Thread.currentThread().getName() + " daemon=" + Thread.currentThread().isDaemon())
                       .get(10, TimeUnit.SECONDS);
    }

    @Test
    void closeShouldLetQueuedRevisionWritesFinish() {
        executors = KernelExecutors.create(1, 1);
        List<Integer> written = new CopyOnWriteArrayList<>();
        for (var i = 0; i < 5; i++) {
            var revision = i;
            executors.threadPools().revisionWrites().execute(() -> {
                sleep(Duration.ofMillis(50));
                written.add(revision);
            });
        }

        executors.close();

        assertThat(written).containsExactly(0, 1, 2, 3, 4);
        assertThat(executors.threadPools().revisionWrites().isTerminated()).isTrue();
        assertThat(executors.threadPools().indexUpdates().isTerminated()).isTrue();
        assertThat(executors.threadPools().eventPurges().isTerminated()).isTrue();
    }

    @Test
    void closeShouldCancelPeriodicEventPurges() {
        executors = KernelExecutors.create(1, 1);
        executors.threadPools().eventPurges().scheduleAtFixedRate(() -> { }, 0, 10, TimeUnit.MILLISECONDS);

        executors.close();

        assertThat(executors.threadPools().eventPurges().isTerminated()).isTrue();
    }

    @Test
    void closeShouldBeRepeatable() {
        executors = KernelExecutors.create(1, 1);

        executors.close();
        executors.close();

        assertThat(executors.threadPools().indexUpdates().isShutdown()).isTrue();
    }

    @Test
    void shouldRejectEmptyPools() {
        assertThatIllegalArgumentException().isThrownBy(() -> KernelExecutors.create(0, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> KernelExecutors.create(1, 0));
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
