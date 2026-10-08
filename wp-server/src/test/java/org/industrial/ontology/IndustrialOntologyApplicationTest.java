package org.industrial.ontology;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Duration;
import org.industrial.ontology.app.persistence.MongoMigration;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.app.project.KernelExecutors;
import org.industrial.ontology.app.project.ProjectRuntimeProperties;
import org.industrial.ontology.app.project.persistence.MongoProjectDetailsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Smoke test for the assembled application: guards the module wiring and the
 * actuator exposure from docs/00 §8 before any feature code exists.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.health.show-components=always")
class IndustrialOntologyApplicationTest {

    @TempDir
    static Path dataDirectory;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ApplicationContext application;

    @DynamicPropertySource
    static void dataDirectoryAndMongo(DynamicPropertyRegistry registry) {
        // The default /srv/webprotege is not writable on developer machines and CI runners.
        registry.add("webprotege.data-directory", () -> dataDirectory.resolve("data").toString());
        // A mongo:7 container where Docker runs, the in-memory server otherwise (MongoTestServer).
        registry.add("spring.data.mongodb.uri", () -> MongoTestServer.uri(MongoTestServer.uniqueDatabase()));
    }

    @Test
    void healthEndpointIsUp() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }

    @Test
    void healthReportsWritableDataDirectory() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getBody()).contains("\"dataDirectory\":{\"status\":\"UP\"}");
    }

    /**
     * Spring Boot's Mongo health indicator comes with the Mongo starter that wp-app brings (docs/01 §7, 07 7-1).
     */
    @Test
    void healthReportsMongo() {
        ResponseEntity<String> response = rest.getForEntity("/actuator/health", String.class);

        assertThat(response.getBody()).contains("\"mongo\":{\"status\":\"UP\"}");
    }

    @Test
    void persistenceIsWiredToTheConfiguredDatabase() {
        assertThat(application.getBean(MongoProjectDetailsRepository.class)).isNotNull();
        assertThat(application.getBean(MongoMigration.class)).isNotNull();
        assertThat(application.getBean(MongoTemplate.class).getDb().getName()).startsWith("wp_test_");
    }

    @Test
    void projectRuntimeIsConfiguredFromApplicationYml() {
        assertThat(application.getBean(KernelExecutors.class)).isNotNull();

        ProjectRuntimeProperties properties = application.getBean(ProjectRuntimeProperties.class);
        assertThat(properties.project().dormantTime()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.events().retention()).isEqualTo(Duration.ofMinutes(10));
        assertThat(properties.kernel().indexUpdateThreads()).isEqualTo(10);
        assertThat(properties.kernel().revisionWriteThreads()).isEqualTo(4);
    }
}
