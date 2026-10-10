package org.industrial.ontology.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import org.bson.Document;
import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.api.OidcTestServer;
import org.industrial.ontology.app.access.persistence.RoleAssignmentDocument;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.user.persistence.UserRecordDocument;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The S5 acceptance (docs/07): a Keycloak token, an API key and no credentials each give the right user or 401; a
 * service call without the permission gives 403 {@code PERMISSION_DENIED}. Also the admin realm role (07 6-2), the
 * compatibility paths on the same chain (6-6) and the precedence of a JWT over an API key.
 */
class AuthenticationIT extends ApiIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private MongoTemplate mongo;

    @Test
    void aTokenShouldAuthenticateItsUserAndRegisterItOnFirstSight() {
        var response = get("/api/v1/me", bearer("editor"));

        assertThat(response.getStatusCode()).as(OidcTestServer.backend()).isEqualTo(HttpStatus.OK);
        var me = json(response);
        assertThat(me.get("userId").asText()).isEqualTo("editor");
        assertThat(me.get("displayName").asText()).isEqualTo("Editor Dev");
        assertThat(me.get("email").asText()).isEqualTo("editor@example.test");
        assertThat(me.get("applicationActions")).extracting(JsonNode::asText)
                                                .doesNotContain("EditApplicationSettings");
        var stored = mongo.getDb().getCollection(UserRecordDocument.COLLECTION)
                          .find(new Document("_id", "editor")).first();
        assertThat(stored).isEqualTo(new Document("_id", "editor").append("realName", "Editor Dev")
                                                                   .append("emailAddress", "editor@example.test"));
    }

    @Test
    void theAdminRealmRoleShouldMakeTheUserASystemAdminForTheRequestOnly() {
        var me = json(get("/api/v1/me", bearer("admin")));

        assertThat(me.get("applicationActions")).extracting(JsonNode::asText)
                                                .contains("EditApplicationSettings", "ViewAnyUserDetails",
                                                          "RebuildPermissions");
        assertThat(get("/api/v1/admin/settings", bearer("admin")).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mongo.getDb().getCollection(RoleAssignmentDocument.COLLECTION)
                        .countDocuments(new Document("userName", "admin"))).isZero();
    }

    @Test
    void anApiKeyShouldAuthenticateItsUser() {
        var key = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "integration").apiKey();

        var response = get("/api/v1/me", apiKey(key));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(response).get("userId").asText()).isEqualTo("viewer");
    }

    @Test
    void aRequestWithoutCredentialsShouldGet401() {
        var response = get("/api/v1/me", null);

        assertUnauthenticated(response);
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).startsWith("Bearer");
        assertThat(json(response).get("instance").asText()).isEqualTo("/api/v1/me");
    }

    @Test
    void anUnknownApiKeyShouldGet401() {
        var response = get("/api/v1/me", "ApiKey not-a-key");

        assertUnauthenticated(response);
        assertThat(json(response).get("detail").asText()).isEqualTo("Unknown API key");
    }

    @Test
    void aTamperedTokenShouldGet401() {
        var token = bearer("editor");
        var tampered = token.substring(0, token.length() - 4) + (token.endsWith("AAAA") ? "BBBB" : "AAAA");

        var response = get("/api/v1/me", tampered);

        assertUnauthenticated(response);
        assertThat(response.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE)).contains("invalid_token");
    }

    @Test
    void aTokenOfAnotherIssuerShouldGet401() throws Exception {
        var key = new RSAKeyGenerator(2048).keyID("other").generate();
        var claims = new JWTClaimsSet.Builder().issuer("http://127.0.0.1:1/realms/other")
                                               .audience(OidcTestServer.AUDIENCE)
                                               .claim("preferred_username", "editor")
                                               .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                                               .build();

        assertUnauthenticated(get("/api/v1/me", "Bearer " + OidcTestServer.sign(claims, key)));
    }

    @Test
    void aServiceCallWithoutThePermissionShouldGet403() {
        var response = get("/api/v1/admin/settings", bearer("viewer"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        var problem = json(response);
        assertThat(problem.get("code").asText()).isEqualTo("PERMISSION_DENIED");
        assertThat(problem.get("status").asInt()).isEqualTo(403);
        assertThat(problem.get("detail").asText()).contains("EditApplicationSettings");
    }

    @Test
    void usersShouldManageTheirOwnApiKeys() {
        var created = exchange(HttpMethod.POST, "/api/v1/me/api-keys", bearer("editor"),
                               Map.of("purpose", "CI pipeline"));
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        var newKey = json(created);
        var apiKeyId = newKey.get("apiKeyId").asText();
        var key = "ApiKey " + newKey.get("apiKey").asText();
        assertThat(created.getHeaders().getLocation()).hasPath("/api/v1/me/api-keys/" + apiKeyId);
        assertThat(created.getHeaders().getCacheControl()).isEqualTo("no-store");
        assertThat(Instant.parse(newKey.get("createdAt").asText())).isBeforeOrEqualTo(Instant.now());

        assertThat(json(get("/api/v1/me", key)).get("userId").asText()).isEqualTo("editor");
        var listed = json(get("/api/v1/me/api-keys", key));
        assertThat(listed).anySatisfy(item -> {
            assertThat(item.get("apiKeyId").asText()).isEqualTo(apiKeyId);
            assertThat(item.get("purpose").asText()).isEqualTo("CI pipeline");
            assertThat(item.has("apiKey")).isFalse();
        });

        var revoked = exchange(HttpMethod.DELETE, "/api/v1/me/api-keys/" + apiKeyId, bearer("editor"), null);
        assertThat(revoked.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertUnauthenticated(get("/api/v1/me", key));
        var again = exchange(HttpMethod.DELETE, "/api/v1/me/api-keys/" + apiKeyId, bearer("editor"), null);
        assertThat(again.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(json(again).get("code").asText()).isEqualTo("API_KEY_NOT_FOUND");
    }

    @Test
    void aBlankPurposeShouldGet400() {
        var response = exchange(HttpMethod.POST, "/api/v1/me/api-keys", bearer("editor"), Map.of("purpose", " "));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(json(response).get("code").asText()).isEqualTo("INVALID_REQUEST");
    }

    /**
     * {@code /download} is the real download (S6): only the editor may download the editor's project, so 200 and 403
     * tell whose key authenticated the request.
     */
    @Test
    void theCompatibilityPathsShouldUseTheSameChain() {
        var key = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "legacy script").apiKey();
        var editorsKey = apiKeyService.generateApiKeyForUser(UserId.getUserId("editor"), "download link").apiKey();
        var download = "/download?project=" + createProject("editor", "Compatibility chain").getId();

        assertThat(json(get("/data/projects", apiKey(key))).get("caller").asText()).isEqualTo("viewer");
        assertThat(json(get("/data/projects", bearer("editor"))).get("caller").asText()).isEqualTo("editor");
        assertUnauthenticated(get("/data/projects", null));
        assertThat(get(download + "&apiKey=" + editorsKey.getKey(), null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get(download + "&apiKey=" + key.getKey(), null).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertUnauthenticated(get(download, null));
    }

    @Test
    void onlyDownloadsShouldTakeTheApiKeyFromTheQuery() {
        var key = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "download link").apiKey();

        assertUnauthenticated(get("/api/v1/me?apiKey=" + key.getKey(), null));
        assertUnauthenticated(get("/data/projects?apiKey=" + key.getKey(), null));
        assertUnauthenticated(get("/download/other?apiKey=" + key.getKey(), null));
    }

    @Test
    void anApiKeyShouldNotManageApiKeys() {
        var generated = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "leaked");
        var key = apiKey(generated.apiKey());

        var created = exchange(HttpMethod.POST, "/api/v1/me/api-keys", key, Map.of("purpose", "copy"));
        var revoked = exchange(HttpMethod.DELETE, "/api/v1/me/api-keys/" + generated.details().apiKeyId().getId(),
                               key, null);

        for (var response : List.of(created, revoked)) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(json(response).get("code").asText()).isEqualTo("PERMISSION_DENIED");
        }
        assertThat(get("/api/v1/me/api-keys", key).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(apiKeyService.getUserIdForApiKey(generated.apiKey())).contains(UserId.getUserId("viewer"));
    }

    /**
     * An asynchronous response is dispatched a second time, without the API key filter.
     */
    @Test
    void anApiKeyShouldStayAuthenticatedForAsyncResponses() {
        var key = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "async").apiKey();

        var response = get("/data/async", apiKey(key));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(response).get("caller").asText()).isEqualTo("viewer");
    }

    @Test
    void aTokenShouldWinOverAnApiKey() {
        var viewersKey = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "precedence").apiKey();
        var download = "/download?project=" + createProject("editor", "Token precedence").getId();

        var response = get(download + "&apiKey=" + viewersKey.getKey(), bearer("editor"));

        // The viewer may not download the editor's project
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void administratorsShouldListAndChangeTheApplication() {
        get("/api/v1/me", bearer("editor"));

        var users = json(get("/api/v1/admin/users?q=EDIT", bearer("admin")));
        assertThat(users).anySatisfy(user -> assertThat(user.get("userId").asText()).isEqualTo("editor"));
        assertThat(get("/api/v1/admin/users", bearer("viewer")).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        var original = json(get("/api/v1/admin/settings", bearer("admin")));
        var settings = (ObjectNode) original.deepCopy();
        settings.put("applicationName", "Ontology Hub").put("projectCreationSetting",
                                                            "EMPTY_PROJECT_CREATION_ALLOWED");
        try {
            var changed = exchange(HttpMethod.PUT, "/api/v1/admin/settings", bearer("admin"), settings);
            assertThat(changed.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(json(changed).get("applicationName").asText()).isEqualTo("Ontology Hub");
            assertThat(json(get("/api/v1/me", bearer("viewer"))).get("applicationActions"))
                    .extracting(JsonNode::asText).contains("CreateEmptyProject");

            settings.putNull("applicationLocation");
            var invalid = exchange(HttpMethod.PUT, "/api/v1/admin/settings", bearer("admin"), settings);
            assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(json(invalid).get("code").asText()).isEqualTo("INVALID_REQUEST");
        } finally {
            exchange(HttpMethod.PUT, "/api/v1/admin/settings", bearer("admin"), original);
        }
    }

    @Test
    void theLocalLoginShouldBeOffByDefault() {
        var response = rest.postForEntity("/login?username=admin&password=admin", null, String.class);

        assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                                                  HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).doesNotContain("access_token");
    }

    @Test
    void theOpenApiDocumentShouldListTheEndpoints() {
        var response = get("/v3/api-docs", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(json(response).get("paths").fieldNames()).toIterable()
                .contains("/api/v1/me", "/api/v1/me/api-keys", "/api/v1/me/api-keys/{apiKeyId}",
                          "/api/v1/admin/settings", "/api/v1/admin/users");
    }

    private void assertUnauthenticated(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(json(response).get("code").asText()).isEqualTo("UNAUTHENTICATED");
        assertThat(List.of(response.getBody())).noneMatch(body -> body.contains("\"userId\""));
    }
}
