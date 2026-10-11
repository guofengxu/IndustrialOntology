package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessRepository;
import org.industrial.ontology.app.user.persistence.UserActivityRepository;
import org.industrial.ontology.app.webhook.persistence.WebhookRepository;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * The project services of stage S6 (docs/01 §5.1): projects, their settings, uploads and downloads. They load
 * projects, so they exist only where the {@link ProjectRegistry} does, that is in wp-server, which has a
 * {@code DataDirectoryLayout}; wp-cli has neither. The sharing and permission services do not load projects and are
 * registered with the access manager ({@code AccessAutoConfiguration}).
 */
@AutoConfiguration(after = ProjectRuntimeAutoConfiguration.class)
@ConditionalOnBean(ProjectRegistry.class)
public class ProjectServicesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public UploadedProjectImporter uploadedProjectImporter(DataDirectoryLayout dataDirectoryLayout,
                                                           KernelExecutors kernelExecutors) {
        return new UploadedProjectImporter(dataDirectoryLayout, kernelExecutors);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectService projectService(AccessManager accessManager,
                                         MongoProjectDetailsRepository projectDetailsRepository,
                                         ProjectAccessRepository projectAccessRepository,
                                         UserActivityRepository userActivityRepository,
                                         ProjectRegistry projectRegistry,
                                         UploadedProjectImporter uploadedProjectImporter) {
        return new ProjectService(accessManager,
                                  projectDetailsRepository,
                                  projectAccessRepository,
                                  userActivityRepository,
                                  projectRegistry,
                                  uploadedProjectImporter,
                                  Clock.systemUTC());
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectSettingsService projectSettingsService(AccessManager accessManager,
                                                         MongoProjectDetailsRepository projectDetailsRepository,
                                                         WebhookRepository webhookRepository,
                                                         MongoPrefixDeclarationsStore prefixDeclarationsStore,
                                                         MongoEntityCrudKitSettingsRepository crudKitSettings,
                                                         ProjectRegistry projectRegistry) {
        return new ProjectSettingsService(accessManager,
                                          projectDetailsRepository,
                                          webhookRepository,
                                          prefixDeclarationsStore,
                                          crudKitSettings,
                                          projectRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public UploadService uploadService(AccessManager accessManager,
                                       ApplicationPreferencesRepository applicationPreferencesRepository,
                                       DataDirectoryLayout dataDirectoryLayout) {
        return new UploadService(accessManager, applicationPreferencesRepository, dataDirectoryLayout);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectDownloadService projectDownloadService(AccessManager accessManager,
                                                         MongoProjectDetailsRepository projectDetailsRepository,
                                                         MongoPrefixDeclarationsStore prefixDeclarationsStore,
                                                         ProjectRegistry projectRegistry,
                                                         DataDirectoryLayout dataDirectoryLayout) {
        return new ProjectDownloadService(accessManager,
                                          projectDetailsRepository,
                                          prefixDeclarationsStore,
                                          projectRegistry,
                                          dataDirectoryLayout);
    }
}
