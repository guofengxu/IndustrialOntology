package org.industrial.ontology.app.project;

import org.industrial.ontology.kernel.api.project.BuiltInPrefixDeclarations;
import org.industrial.ontology.kernel.project.BuiltInPrefixDeclarationsLoader;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.project.ProjectContextFactory;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Wires the project runtime (docs/01 §2, §3.4, §7): the shared {@link KernelExecutors}, the
 * {@link ProjectContextFactory} and the {@link ProjectRegistry}.
 * <p>
 * The factory needs wp-app's {@link ProjectPorts} ({@code MongoProjectPorts}, registered by
 * {@code ProjectPortsAutoConfiguration} before this one) and wp-server's {@link DataDirectoryLayout}. Without either
 * bean the factory and the registry are not created; wp-cli, which has no data directory, runs without them. This is
 * an auto-configuration because {@link ConditionalOnBean} only sees beans that are already registered, and
 * auto-configurations are processed after the component-scanned configuration. For the same reason the conditions
 * are on the bean methods rather than on a nested configuration class: wp-server scans {@code org.industrial.ontology},
 * and component scanning picks up a nested {@code @Configuration} class of an auto-configuration as a configuration
 * of its own, which is then processed before the ports exist.
 * <p>
 * Shutdown (docs/01 §7): the registry depends on the factory and the factory on the executors, so Spring destroys
 * them in that order. {@link ProjectRegistry#closeAll()} waits until every project's pending revisions are written,
 * and only then does {@link KernelExecutors#close()} stop the pools. With wp-server's graceful shutdown, requests in
 * progress finish before either runs.
 */
@AutoConfiguration
@EnableConfigurationProperties(ProjectRuntimeProperties.class)
public class ProjectRuntimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KernelExecutors kernelExecutors(ProjectRuntimeProperties properties) {
        return KernelExecutors.create(properties.kernel().indexUpdateThreads(),
                                      properties.kernel().revisionWriteThreads());
    }

    /**
     * The built-in prefixes, which an administrator can override with a file in the data directory.
     */
    @Bean
    @ConditionalOnBean({DataDirectoryLayout.class, ProjectPorts.class})
    @ConditionalOnMissingBean
    public BuiltInPrefixDeclarations builtInPrefixDeclarations(DataDirectoryLayout dataDirectoryLayout) {
        return new BuiltInPrefixDeclarationsLoader(dataDirectoryLayout.getOverridableFileFactory())
                .getBuiltInPrefixDeclarations();
    }

    @Bean
    @ConditionalOnBean({DataDirectoryLayout.class, ProjectPorts.class})
    @ConditionalOnMissingBean
    public ProjectContextFactory projectContextFactory(DataDirectoryLayout dataDirectoryLayout,
                                                       KernelExecutors kernelExecutors,
                                                       ProjectPorts projectPorts,
                                                       BuiltInPrefixDeclarations builtInPrefixDeclarations,
                                                       ProjectRuntimeProperties properties) {
        return new ProjectContextFactory(dataDirectoryLayout,
                                         kernelExecutors.threadPools(),
                                         projectPorts,
                                         builtInPrefixDeclarations,
                                         properties.events().retention());
    }

    @Bean(destroyMethod = "closeAll")
    @ConditionalOnBean({DataDirectoryLayout.class, ProjectPorts.class})
    @ConditionalOnMissingBean
    public ProjectRegistry projectRegistry(ProjectContextFactory projectContextFactory,
                                           ProjectRuntimeProperties properties) {
        return new ProjectRegistry(projectContextFactory, properties.project().dormantTime());
    }
}
