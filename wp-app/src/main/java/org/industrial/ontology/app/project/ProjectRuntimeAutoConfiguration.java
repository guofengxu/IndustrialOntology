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
import org.springframework.context.annotation.Configuration;

/**
 * Wires the project runtime (docs/01 §2, §3.4, §7): the shared {@link KernelExecutors}, the
 * {@link ProjectContextFactory} and the {@link ProjectRegistry}.
 * <p>
 * The factory needs wp-app's {@link ProjectPorts}, which are built on the Mongo repositories and the access manager
 * (stages S4 and S5), and wp-server's {@link DataDirectoryLayout}. Until both beans exist the factory and the registry
 * are not created, and the server starts without them. This is an auto-configuration because
 * {@link ConditionalOnBean} only sees beans that are already registered, and auto-configurations are processed after
 * the component-scanned configuration.
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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean({DataDirectoryLayout.class, ProjectPorts.class})
    static class ProjectRegistryConfiguration {

        /**
         * The built-in prefixes, which an administrator can override with a file in the data directory.
         */
        @Bean
        @ConditionalOnMissingBean
        public BuiltInPrefixDeclarations builtInPrefixDeclarations(DataDirectoryLayout dataDirectoryLayout) {
            return new BuiltInPrefixDeclarationsLoader(dataDirectoryLayout.getOverridableFileFactory())
                    .getBuiltInPrefixDeclarations();
        }

        @Bean
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
        @ConditionalOnMissingBean
        public ProjectRegistry projectRegistry(ProjectContextFactory projectContextFactory,
                                               ProjectRuntimeProperties properties) {
            return new ProjectRegistry(projectContextFactory, properties.project().dormantTime());
        }
    }
}
