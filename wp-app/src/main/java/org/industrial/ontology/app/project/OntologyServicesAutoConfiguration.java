package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.entity.BulkEditService;
import org.industrial.ontology.app.entity.EntityRenderingService;
import org.industrial.ontology.app.entity.EntityService;
import org.industrial.ontology.app.entity.IndividualsService;
import org.industrial.ontology.app.frame.AxiomsService;
import org.industrial.ontology.app.frame.FrameService;
import org.industrial.ontology.app.frame.ManchesterSyntaxService;
import org.industrial.ontology.app.frame.OntologyService;
import org.industrial.ontology.app.hierarchy.HierarchyService;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.search.SearchService;
import org.industrial.ontology.app.search.SearchSettingsService;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * The ontology editing and search services of stage S7 (docs/01 §5.1): frames, Manchester syntax, ontologies and
 * axioms, entities, hierarchies, individuals and search. They work on loaded projects, so like the project services
 * they exist only where the {@link ProjectRegistry} does (wp-server).
 */
@AutoConfiguration(after = ProjectRuntimeAutoConfiguration.class)
@ConditionalOnBean(ProjectRegistry.class)
public class OntologyServicesAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProjectContexts projectContexts(AccessManager accessManager,
                                           MongoProjectDetailsRepository projectDetailsRepository,
                                           ProjectRegistry projectRegistry) {
        return new ProjectContexts(accessManager, projectDetailsRepository, projectRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public FrameService frameService(ProjectContexts projectContexts) {
        return new FrameService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public ManchesterSyntaxService manchesterSyntaxService(ProjectContexts projectContexts) {
        return new ManchesterSyntaxService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public OntologyService ontologyService(ProjectContexts projectContexts) {
        return new OntologyService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxiomsService axiomsService(ProjectContexts projectContexts) {
        return new AxiomsService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityService entityService(ProjectContexts projectContexts,
                                       DiscussionThreadRepository discussionThreadRepository) {
        return new EntityService(projectContexts, discussionThreadRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityRenderingService entityRenderingService(ProjectContexts projectContexts) {
        return new EntityRenderingService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public BulkEditService bulkEditService(ProjectContexts projectContexts) {
        return new BulkEditService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public IndividualsService individualsService(ProjectContexts projectContexts) {
        return new IndividualsService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public HierarchyService hierarchyService(ProjectContexts projectContexts) {
        return new HierarchyService(projectContexts);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchService searchService(ProjectContexts projectContexts,
                                       EntitySearchFilterRepository entitySearchFilterRepository) {
        return new SearchService(projectContexts, entitySearchFilterRepository);
    }

    @Bean
    @ConditionalOnMissingBean
    public SearchSettingsService searchSettingsService(ProjectContexts projectContexts,
                                                       EntitySearchFilterRepository entitySearchFilterRepository) {
        return new SearchSettingsService(projectContexts, entitySearchFilterRepository);
    }
}
