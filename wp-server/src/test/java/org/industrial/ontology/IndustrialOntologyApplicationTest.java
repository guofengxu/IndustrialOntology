package org.industrial.ontology;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.time.Duration;
import org.industrial.ontology.app.project.KernelExecutors;
import org.industrial.ontology.app.project.ProjectRuntimeProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
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
    static void dataDirectory(DynamicPropertyRegistry registry) {
        // The default /srv/webprotege is not writable on developer machines and CI runners.
        registry.add("webprotege.data-directory", () -> dataDirectory.resolve("data").toString());
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
