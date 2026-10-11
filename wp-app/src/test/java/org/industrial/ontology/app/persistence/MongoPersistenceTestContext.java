package org.industrial.ontology.app.persistence;

import com.mongodb.client.MongoDatabase;
import org.industrial.ontology.app.access.AccessAutoConfiguration;
import org.industrial.ontology.app.project.ProjectPortsAutoConfiguration;
import org.industrial.ontology.app.project.ProjectRuntimeAutoConfiguration;
import org.industrial.ontology.app.project.ProjectServicesAutoConfiguration;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.annotation.Configurations;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A Spring context with Spring Boot's Mongo auto-configuration and {@link MongoPersistenceAutoConfiguration}, on a
 * database of its own on {@link MongoTestServer}; the database is dropped when the context is closed. One context per
 * test class keeps the tests fast. It has no Spring annotations, so a component scan of the test jar (wp-server's
 * tests have it on the classpath) never picks it up.
 */
public final class MongoPersistenceTestContext implements AutoCloseable {

    private final AnnotationConfigApplicationContext context;

    private MongoPersistenceTestContext(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    public static MongoPersistenceTestContext start(String... properties) {
        return start(List.of(), properties);
    }

    /**
     * The persistence with the services of {@link AccessAutoConfiguration} on top: the access manager and the user,
     * API key and application settings services.
     */
    public static MongoPersistenceTestContext startWithServices(String... properties) {
        return start(List.of(AccessAutoConfiguration.class), properties);
    }

    /**
     * What wp-server has of wp-app (S6): the persistence, the access services, {@code MongoProjectPorts}, the project
     * runtime over the data directory, and the project services.
     */
    public static MongoPersistenceTestContext startWithProjects(Path dataDirectory, String... properties) {
        var context = new AnnotationConfigApplicationContext();
        context.registerBean(DataDirectoryLayout.class, () -> new DataDirectoryLayout(dataDirectory));
        return start(context,
                     List.of(AccessAutoConfiguration.class,
                             ProjectPortsAutoConfiguration.class,
                             ProjectRuntimeAutoConfiguration.class,
                             ProjectServicesAutoConfiguration.class),
                     properties);
    }

    private static MongoPersistenceTestContext start(List<Class<?>> moreAutoConfigurations, String... properties) {
        return start(new AnnotationConfigApplicationContext(), moreAutoConfigurations, properties);
    }

    private static MongoPersistenceTestContext start(AnnotationConfigApplicationContext context,
                                                     List<Class<?>> moreAutoConfigurations,
                                                     String... properties) {
        TestPropertyValues.of("spring.data.mongodb.uri=" + MongoTestServer.uri(MongoTestServer.uniqueDatabase()))
                          .and(properties)
                          .applyTo(context);
        // In the order Spring Boot sorts them: the persistence configuration goes before MongoDataAutoConfiguration.
        var autoConfigurations = new ArrayList<Class<?>>(List.of(MongoAutoConfiguration.class,
                                                                 MongoDataAutoConfiguration.class,
                                                                 MongoPersistenceAutoConfiguration.class));
        autoConfigurations.addAll(moreAutoConfigurations);
        context.register(Configurations.getClasses(AutoConfigurations.of(autoConfigurations.toArray(Class<?>[]::new))));
        context.refresh();
        return new MongoPersistenceTestContext(context);
    }

    public <T> T bean(Class<T> type) {
        return context.getBean(type);
    }

    public MongoTemplate mongoTemplate() {
        return context.getBean(MongoTemplate.class);
    }

    public MongoDatabase database() {
        return mongoTemplate().getDb();
    }

    /**
     * Drops every collection and creates the legacy indexes again, as the application does at startup. (Deleting the
     * documents instead trips over mongo-java-server's multikey unique indexes when an indexed array is empty.)
     */
    public void clear() {
        var database = database();
        for (var name : database.listCollectionNames()) {
            database.getCollection(name).drop();
        }
        bean(MongoIndexes.class).ensureIndexes(false);
    }

    @Override
    public void close() {
        try {
            database().drop();
        } finally {
            context.close();
        }
    }
}
