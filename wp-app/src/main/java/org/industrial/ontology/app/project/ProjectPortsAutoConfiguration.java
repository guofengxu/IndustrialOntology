package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessAutoConfiguration;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.tag.persistence.EntityTagsRepository;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.app.watch.persistence.WatchRepository;
import org.industrial.ontology.kernel.project.ProjectPorts;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Registers wp-app's {@link MongoProjectPorts} (stage S6). It runs before {@link ProjectRuntimeAutoConfiguration},
 * whose {@code ProjectContextFactory} and {@code ProjectRegistry} are created only when a {@link ProjectPorts} bean
 * exists; in wp-server, which also has a {@code DataDirectoryLayout}, they now always are. Tests that bring their
 * own ports keep them.
 */
@AutoConfiguration(after = AccessAutoConfiguration.class, before = ProjectRuntimeAutoConfiguration.class)
public class ProjectPortsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProjectPorts.class)
    public MongoProjectPorts projectPorts(MongoProjectDetailsRepository projectDetailsRepository,
                                          MongoPrefixDeclarationsStore prefixDeclarationsStore,
                                          MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository,
                                          DiscussionThreadRepository discussionThreadRepository,
                                          TagRepository tagRepository,
                                          EntityTagsRepository entityTagsRepository,
                                          WatchRepository watchRepository,
                                          EntitySearchFilterRepository entitySearchFilterRepository,
                                          AccessManager accessManager) {
        return new MongoProjectPorts(projectDetailsRepository,
                                     prefixDeclarationsStore,
                                     entityCrudKitSettingsRepository,
                                     discussionThreadRepository,
                                     tagRepository,
                                     entityTagsRepository,
                                     watchRepository,
                                     entitySearchFilterRepository,
                                     accessManager);
    }
}
