package org.industrial.ontology.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.industrial.ontology.domain.core.ApiKey;
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

/**
 * The security tests run the HTTP server on a random port, with the Mongo of {@link MongoTestServer} and the issuer
 * of {@link OidcTestServer}. Subclasses with other {@code webprotege.auth} settings get contexts of their own; all
 * of them share one database, so tests use their own users or do not depend on what others stored.
 */
@SpringBootTest(classes = ApiTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class ApiIntegrationTest {

    private static final String DATABASE = MongoTestServer.uniqueDatabase();

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected ObjectMapper objectMapper;

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
