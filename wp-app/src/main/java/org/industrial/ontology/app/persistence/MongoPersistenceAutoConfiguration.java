package org.industrial.ontology.app.persistence;

import org.industrial.ontology.app.access.persistence.RoleAssignmentRepository;
import org.industrial.ontology.app.admin.persistence.ApplicationPreferencesRepository;
import org.industrial.ontology.app.apikey.persistence.UserApiKeyRepository;
import org.industrial.ontology.app.form.persistence.MongoEntityFormRepository;
import org.industrial.ontology.app.form.persistence.MongoEntityFormSelectorRepository;
import org.industrial.ontology.app.issues.persistence.DiscussionThreadRepository;
import org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorRepository;
import org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutRepository;
import org.industrial.ontology.app.project.persistence.MongoEntityCrudKitSettingsRepository;
import org.industrial.ontology.app.project.persistence.MongoPrefixDeclarationsStore;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.industrial.ontology.app.project.persistence.ProjectAccessRepository;
import org.industrial.ontology.app.search.persistence.EntitySearchFilterRepository;
import org.industrial.ontology.app.tag.persistence.EntityTagsRepository;
import org.industrial.ontology.app.tag.persistence.TagRepository;
import org.industrial.ontology.app.user.persistence.UserActivityRepository;
import org.industrial.ontology.app.user.persistence.UserRecordRepository;
import org.industrial.ontology.app.watch.persistence.WatchRepository;
import org.industrial.ontology.app.webhook.persistence.WebhookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * Persistence on the legacy {@code webprotege} database (docs/01 §5.3): the Spring Data mapping, the repositories of
 * the 19 collections, and the legacy indexes.
 * <p>
 * The mapping replaces Spring Boot's {@link MappingMongoConverter}, so this runs before
 * {@link MongoDataAutoConfiguration}, whose converter and conversions then back off:
 * <ul>
 *     <li>no type key: Spring Data's default writes a {@code _class} field into every document, which the legacy
 *     documents do not have; reading ignores unmapped fields anyway, including Morphia's old {@code className}
 *     (docs/07 5.3-2);</li>
 *     <li>{@link OwlEntityMongoCodec} for {@code OWLEntity} properties and query values (5.3-3).</li>
 * </ul>
 * The database comes from {@code spring.data.mongodb.uri}; the legacy name is {@code webprotege}.
 * <p>
 * The legacy repositories created their indexes when they were constructed. Here {@link MongoIndexes} creates the
 * missing ones once all singletons exist, unless {@code webprotege.mongo.ensure-indexes} is {@code false} (wp-cli
 * turns it off, so that {@code migrate-mongo --dry-run} changes nothing). A database that cannot be reached is
 * logged and does not stop startup; the health endpoint reports it.
 */
@AutoConfiguration(before = MongoDataAutoConfiguration.class)
@ConditionalOnClass(MongoTemplate.class)
public class MongoPersistenceAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MongoPersistenceAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(OwlEntityMongoCodec.converters());
    }

    @Bean
    @ConditionalOnMissingBean(MongoConverter.class)
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory databaseFactory,
                                                       MongoMappingContext mappingContext,
                                                       MongoCustomConversions conversions) {
        var converter = new MappingMongoConverter(new DefaultDbRefResolver(databaseFactory), mappingContext);
        converter.setCustomConversions(conversions);
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public JacksonDocumentMapper jacksonDocumentMapper() {
        return new JacksonDocumentMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoIndexes mongoIndexes(MongoDatabaseFactory databaseFactory) {
        return new MongoIndexes(databaseFactory.getMongoDatabase());
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoMigration mongoMigration(MongoDatabaseFactory databaseFactory) {
        return new MongoMigration(databaseFactory.getMongoDatabase());
    }

    @Bean
    @ConditionalOnProperty(prefix = "webprotege.mongo", name = "ensure-indexes", matchIfMissing = true)
    public SmartInitializingSingleton mongoIndexesOnStartup(MongoIndexes mongoIndexes) {
        return () -> {
            try {
                var created = mongoIndexes.ensureIndexes(false)
                                          .stream()
                                          .filter(result -> result.outcome() == MongoIndexes.Outcome.CREATED)
                                          .count();
                logger.info("Legacy Mongo indexes checked, {} created", created);
            } catch (RuntimeException e) {
                logger.error("Cannot check the legacy Mongo indexes; run wp-cli migrate-mongo once the database is "
                                     + "reachable", e);
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public UserRecordRepository userRecordRepository(MongoTemplate mongo) {
        return new UserRecordRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserActivityRepository userActivityRepository(MongoTemplate mongo) {
        return new UserActivityRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoProjectDetailsRepository projectDetailsRepository(MongoTemplate mongo, JacksonDocumentMapper mapper) {
        return new MongoProjectDetailsRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProjectAccessRepository projectAccessRepository(MongoTemplate mongo) {
        return new ProjectAccessRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public RoleAssignmentRepository roleAssignmentRepository(MongoTemplate mongo) {
        return new RoleAssignmentRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoPrefixDeclarationsStore prefixDeclarationsStore(MongoTemplate mongo, JacksonDocumentMapper mapper) {
        return new MongoPrefixDeclarationsStore(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoEntityCrudKitSettingsRepository entityCrudKitSettingsRepository(MongoTemplate mongo,
                                                                                JacksonDocumentMapper mapper) {
        return new MongoEntityCrudKitSettingsRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PerspectiveDescriptorRepository perspectiveDescriptorRepository(MongoTemplate mongo,
                                                                           JacksonDocumentMapper mapper) {
        return new PerspectiveDescriptorRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public PerspectiveLayoutRepository perspectiveLayoutRepository(MongoTemplate mongo, JacksonDocumentMapper mapper) {
        return new PerspectiveLayoutRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoEntityFormRepository entityFormRepository(MongoTemplate mongo, JacksonDocumentMapper mapper) {
        return new MongoEntityFormRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoEntityFormSelectorRepository entityFormSelectorRepository(MongoTemplate mongo,
                                                                          JacksonDocumentMapper mapper) {
        return new MongoEntityFormSelectorRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public TagRepository tagRepository(MongoTemplate mongo, JacksonDocumentMapper mapper) {
        return new TagRepository(mongo, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityTagsRepository entityTagsRepository(MongoTemplate mongo) {
        return new EntityTagsRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public WatchRepository watchRepository(MongoTemplate mongo) {
        return new WatchRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public DiscussionThreadRepository discussionThreadRepository(MongoTemplate mongo) {
        return new DiscussionThreadRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public WebhookRepository webhookRepository(MongoTemplate mongo) {
        return new WebhookRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public UserApiKeyRepository userApiKeyRepository(MongoTemplate mongo) {
        return new UserApiKeyRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationPreferencesRepository applicationPreferencesRepository(MongoTemplate mongo) {
        return new ApplicationPreferencesRepository(mongo);
    }

    @Bean
    @ConditionalOnMissingBean
    public EntitySearchFilterRepository entitySearchFilterRepository(MongoTemplate mongo,
                                                                     JacksonDocumentMapper mapper) {
        return new EntitySearchFilterRepository(mongo, mapper);
    }
}
