package org.industrial.ontology.app.project;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.app.project.ProjectRuntimeTestSupport.GatedThreadPool;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.project.InMemoryProjectPorts;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.app.project.ProjectRuntimeTestSupport.savedRevisionCount;
import static org.industrial.ontology.app.project.ProjectRuntimeTestSupport.withGatedRevisionWrites;
import static org.industrial.ontology.kernel.project.ProjectKernelFixture.createClass;

/**
 * Graceful shutdown (docs/01 §7, docs/07 7-4): closing the application writes every pending revision. Spring closes
 * the {@link ProjectRegistry} before the {@link KernelExecutors}; in the other order the revision-write pool would stop
 * while revisions are still chained behind each other, and all but the running one would be lost.
 */
class ProjectRuntimeShutdownIT {

    @TempDir
    Path dataDirectory;

    @Test
    void closingTheApplicationShouldWritePendingRevisionsBeforeStoppingThePools() {
        var projectId = ProjectKernelFixture.freshProjectId();
        var revisionWrites = new GatedThreadPool();
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ProjectRuntimeAutoConfiguration.class))
                .withBean(DataDirectoryLayout.class, () -> new DataDirectoryLayout(dataDirectory))
                .withBean(ProjectPorts.class, InMemoryProjectPorts::new)
                .withBean(KernelExecutors.class, () -> withGatedRevisionWrites(revisionWrites))
                .run(application -> {
                    var context = application.getBean(ProjectRegistry.class).get(projectId);
                    for (var i = 1; i <= 5; i++) {
                        createClass(context, "Class" + i, ImmutableSet.of());
                    }
                    // The first revision is saved at once; the other four wait for the pool
                    assertThat(revisionWrites.getCompletedTaskCount()).isZero();

                    var closing = CompletableFuture.runAsync(application::close);
                    assertThatThrownBy(() -> closing.get(500, TimeUnit.MILLISECONDS))
                            .isInstanceOf(TimeoutException.class);
                    revisionWrites.open();
                    closing.get(30, TimeUnit.SECONDS);

                    assertThat(context.isClosed()).isTrue();
                    assertThat(revisionWrites.isTerminated()).isTrue();
                });

        assertThat(savedRevisionCount(dataDirectory, projectId)).isEqualTo(5);
    }
}
