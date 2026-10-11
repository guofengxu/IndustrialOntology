package org.industrial.ontology.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.app.access.AccessManager;
import org.industrial.ontology.app.access.ApplicationResource;
import org.industrial.ontology.app.access.Subject;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.app.project.ProjectService;
import org.industrial.ontology.domain.core.ApiKey;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;

/**
 * The API tests run the HTTP server on a random port, with the Mongo of {@link MongoTestServer} and the issuer of
 * {@link OidcTestServer}. Subclasses with other {@code webprotege.auth} settings get contexts of their own; all of
 * them share one database, so tests use their own users and projects or do not depend on what others stored.
 */
@SpringBootTest(classes = ApiTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiIntegrationTest {

    private static final String DATABASE = MongoTestServer.uniqueDatabase();

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected ProjectService projectService;

    /**
     * Creates an empty project owned by the user through the project service, first adding the project creator role
     * to the user's application roles.
     */
    protected ProjectId createProject(String owner, String displayName) {
        var user = Subject.forUser(owner);
        var roles = new LinkedHashSet<>(accessManager.getAssignedRoles(user, ApplicationResource.get()));
        if (roles.add(BuiltInRole.PROJECT_CREATOR.getRoleId())) {
            accessManager.setAssignedRoles(user, ApplicationResource.get(), roles);
        }
        return projectService.createProject(UserId.getUserId(owner), displayName, "", "en", null).getProjectId();
    }

    @DynamicPropertySource
    static void mongoAndIssuer(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MongoTestServer.uri(DATABASE));
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", OidcTestServer::issuerUri);
        registry.add("spring.security.oauth2.resourceserver.jwt.audiences", () -> OidcTestServer.AUDIENCE);
    }

    protected static String bearer(String userName) {
        return "Bearer " + OidcTestServer.accessToken(userName);
    }

    protected static String apiKey(ApiKey apiKey) {
        return "ApiKey " + apiKey.getKey();
    }

    protected ResponseEntity<String> get(String path, @Nullable String authorization) {
        return exchange(HttpMethod.GET, path, authorization, null);
    }

    protected ResponseEntity<String> exchange(HttpMethod method,
                                              String path,
                                              @Nullable String authorization,
                                              @Nullable Object body) {
        var headers = new HttpHeaders();
        if (authorization != null) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return rest.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }

    protected JsonNode json(ResponseEntity<String> response) {
        try {
            return objectMapper.readTree(response.getBody());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
