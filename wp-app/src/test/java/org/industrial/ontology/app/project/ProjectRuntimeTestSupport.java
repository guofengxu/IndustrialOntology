package org.industrial.ontology.app.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.event.ProjectEventManager;
import org.industrial.ontology.kernel.project.BuiltInPrefixDeclarationsLoader;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.project.InMemoryProjectPorts;
import org.industrial.ontology.kernel.project.ProjectContextFactory;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Builds the kernel the way {@link ProjectRuntimeAutoConfiguration} does, over a test data directory, with the
 * in-memory ports of the kernel tests.
 */
final class ProjectRuntimeTestSupport {

    private ProjectRuntimeTestSupport() {
    }

    static ProjectContextFactory contextFactory(Path dataDirectory, KernelExecutors executors) {
        var layout = new DataDirectoryLayout(dataDirectory);
        return new ProjectContextFactory(layout,
                                         executors.threadPools(),
                                         new InMemoryProjectPorts(),
                                         new BuiltInPrefixDeclarationsLoader(layout.getOverridableFileFactory())
                                                 .getBuiltInPrefixDeclarations(),
                                         ProjectEventManager.DEFAULT_RETENTION);
    }

    /**
     * Reads the project's change history back from disk with a kernel of its own.
     */
    static int savedRevisionCount(Path dataDirectory, ProjectId projectId) {
        var executors = KernelExecutors.create(2, 1);
        try (var context = contextFactory(dataDirectory, executors).create(projectId)) {
            return context.revisionManager().getRevisions().size();
        }
        finally {
            executors.close();
        }
    }

    /**
     * Kernel executors whose revision writes wait until the pool's gate opens, so that a test can shut down while
     * revisions are still pending. A project saves its first revision itself; the later ones go to this pool.
     */
    static KernelExecutors withGatedRevisionWrites(GatedThreadPool revisionWrites) {
        return new KernelExecutors(Executors.newFixedThreadPool(2),
                                   revisionWrites,
                                   Executors.newSingleThreadScheduledExecutor());
    }

    /**
     * A single-thread pool that runs no task until its gate opens.
     */
    static final class GatedThreadPool extends ThreadPoolExecutor {

        private final CountDownLatch gate = new CountDownLatch(1);

        GatedThreadPool() {
            super(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        }

        void open() {
            gate.countDown();
        }

        @Override
        protected void beforeExecute(Thread thread, Runnable task) {
            try {
                if (!gate.await(1, TimeUnit.MINUTES)) {
                    throw new IllegalStateException("The test never opened the gate");
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            super.beforeExecute(thread, task);
        }
    }
}
