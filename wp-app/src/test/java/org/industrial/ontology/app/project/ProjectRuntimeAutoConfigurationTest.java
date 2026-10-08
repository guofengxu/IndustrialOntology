package org.industrial.ontology.app.project;

import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.project.InMemoryProjectPorts;
import org.industrial.ontology.kernel.project.ProjectContextFactory;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ProjectRuntimeAutoConfiguration} provides the kernel executors at once, and the factory and registry only
 * when wp-app's {@link ProjectPorts} and wp-server's {@link DataDirectoryLayout} exist; it binds
 * {@link ProjectRuntimeProperties} with the defaults of docs/00 §8.
 */
class ProjectRuntimeAutoConfigurationTest {

    @TempDir
    Path dataDirectory;

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ProjectRuntimeAutoConfiguration.class));

    private ApplicationContextRunner withKernelCollaborators() {
        return runner.withBean(DataDirectoryLayout.class, () -> new DataDirectoryLayout(dataDirectory))
                     .withBean(ProjectPorts.class, InMemoryProjectPorts::new);
    }

    @Test
    void shouldProvideKernelExecutorsButNoRegistryUntilProjectPortsExist() {
        runner.withBean(DataDirectoryLayout.class, () -> new DataDirectoryLayout(dataDirectory))
              .run(context -> assertThat(context).hasSingleBean(KernelExecutors.class)
                                                 .doesNotHaveBean(ProjectContextFactory.class)
                                                 .doesNotHaveBean(ProjectRegistry.class));
    }

    @Test
    void shouldNotProvideRegistryWithoutDataDirectory() {
        runner.withBean(ProjectPorts.class, InMemoryProjectPorts::new)
              .run(context -> assertThat(context).hasSingleBean(KernelExecutors.class)
                                                 .doesNotHaveBean(ProjectRegistry.class));
    }

    @Test
    void shouldProvideRegistryWithDefaultsWhenPortsAndDataDirectoryExist() {
        withKernelCollaborators().run(context -> {
            assertThat(context).hasSingleBean(ProjectContextFactory.class).hasSingleBean(ProjectRegistry.class);
            assertThat(context.getBean(ProjectRegistry.class).dormantTime()).isEqualTo(Duration.ofHours(1));
            var properties = context.getBean(ProjectRuntimeProperties.class);
            assertThat(properties.events().retention()).isEqualTo(Duration.ofMinutes(10));
            var pools = context.getBean(KernelExecutors.class).threadPools();
            assertThat(((ThreadPoolExecutor) pools.indexUpdates()).getCorePoolSize()).isEqualTo(10);
            assertThat(((ThreadPoolExecutor) pools.revisionWrites()).getCorePoolSize()).isEqualTo(4);
        });
    }

    @Test
    void shouldBindRuntimeProperties() {
        withKernelCollaborators()
                .withPropertyValues("webprotege.project.dormant-time=1s",
                                    "webprotege.events.retention=PT30S",
                                    "webprotege.kernel.index-update-threads=3",
                                    "webprotege.kernel.revision-write-threads=2")
                .run(context -> {
                    assertThat(context.getBean(ProjectRegistry.class).dormantTime()).isEqualTo(Duration.ofSeconds(1));
                    var properties = context.getBean(ProjectRuntimeProperties.class);
                    assertThat(properties.events().retention()).isEqualTo(Duration.ofSeconds(30));
                    var pools = context.getBean(KernelExecutors.class).threadPools();
                    assertThat(((ThreadPoolExecutor) pools.indexUpdates()).getCorePoolSize()).isEqualTo(3);
                    assertThat(((ThreadPoolExecutor) pools.revisionWrites()).getCorePoolSize()).isEqualTo(2);
                });
    }

    @Test
    void shouldRefuseToStartWithoutDormantTime() {
        withKernelCollaborators()
                .withPropertyValues("webprotege.project.dormant-time=0s")
                .run(context -> assertThat(context).hasFailed()
                                                   .getFailure()
                                                   .hasStackTraceContaining("dormant-time must be positive"));
    }

    @Test
    void shouldUseKernelExecutorsDefinedByTheApplication() {
        var executors = KernelExecutors.create(1, 1);
        withKernelCollaborators()
                .withBean(KernelExecutors.class, () -> executors)
                .run(context -> assertThat(context.getBean(KernelExecutors.class)).isSameAs(executors));
    }

    @Test
    void closingTheContextShouldStopThePools() {
        var executors = new AtomicReference<KernelExecutors>();
        withKernelCollaborators().run(context -> executors.set(context.getBean(KernelExecutors.class)));

        assertThat(executors.get().threadPools().indexUpdates().isTerminated()).isTrue();
        assertThat(executors.get().threadPools().revisionWrites().isTerminated()).isTrue();
        assertThat(executors.get().threadPools().eventPurges().isTerminated()).isTrue();
    }
}
