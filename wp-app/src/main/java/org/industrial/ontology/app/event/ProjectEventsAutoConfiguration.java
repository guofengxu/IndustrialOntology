package org.industrial.ontology.app.event;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.project.OntologyServicesAutoConfiguration;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectRegistry;
import org.industrial.ontology.app.project.ProjectRuntimeProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * The project event services of stage S8 (docs/01 §5.1, §5.2): reading events after a tag and streaming them. Like the
 * ontology services they need loaded projects, so they exist only where the {@link ProjectRegistry} does (wp-server).
 * Open streams are ended as shutdown begins ({@link ProjectEventStreamsLifecycle}), and the broadcaster, which depends
 * on the registry, is closed before it.
 */
@AutoConfiguration(after = OntologyServicesAutoConfiguration.class)
@ConditionalOnBean(ProjectRegistry.class)
public class ProjectEventsAutoConfiguration {

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public ProjectEventBroadcaster projectEventBroadcaster(ProjectRegistry projectRegistry,
                                                           AccessManager accessManager,
                                                           ProjectRuntimeProperties properties) {
        var events = properties.events();
        return new ProjectEventBroadcaster(projectRegistry, accessManager, events.heartbeat(),
                                           events.maxSubscriptionsPerUser(), events.maxQueuedBatches());
    }

    /**
     * Ends the streams before the web server's graceful shutdown waits for active requests.
     */
    @Bean
    @ConditionalOnMissingBean
    public ProjectEventStreamsLifecycle projectEventStreamsLifecycle(ProjectEventBroadcaster projectEventBroadcaster) {
        return new ProjectEventStreamsLifecycle(projectEventBroadcaster);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectEventService projectEventService(ProjectContexts projectContexts,
                                                   ProjectEventBroadcaster projectEventBroadcaster) {
        return new ProjectEventService(projectContexts, projectEventBroadcaster);
    }
}
