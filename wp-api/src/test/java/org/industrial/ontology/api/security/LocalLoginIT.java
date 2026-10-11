package org.industrial.ontology.api.security;

import com.fasterxml.jackson.databind.JsonNode;
import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The local fallback login (07 6-4): a local administrator made by {@code create-admin --password} signs in with
 * {@code POST /login} and uses the token like a Keycloak one; Keycloak tokens keep working beside it.
 */
@TestPropertySource(properties = "webprotege.auth.local-login.enabled=true")
class LocalLoginIT extends ApiIntegrationTest {

    private static final UserId ROOT = UserId.getUserId("root");

    private static final String PASSWORD = "local root password";

    @Autowired
    private UserService userService;

    @BeforeEach
    void createLocalAdministrator() {
        userService.createAdministrator(ROOT, "root@example.test", PASSWORD);
    }

    private ResponseEntity<String> login(String userName, String password) {
        var form = new LinkedMultiValueMap<String, String>();
        form.add("username", userName);
        form.add("password", password);
        return rest.postForEntity("/login", new HttpEntity<>(form, formHeaders()), String.class);
    }

    private static HttpHeaders formHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    @Test
    void theLocalAdministratorShouldSignInAndUseTheToken() {
        var response = login("root", PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getCacheControl()).isEqualTo("no-store");
        var token = json(response);
        assertThat(token.get("token_type").asText()).isEqualTo("Bearer");
        assertThat(token.get("expires_in").asLong()).isEqualTo(8 * 3600);

        var me = json(get("/api/v1/me", "Bearer " + token.get("access_token").asText()));
        assertThat(me.get("userId").asText()).isEqualTo("root");
        assertThat(me.get("applicationActions")).extracting(JsonNode::asText).contains("EditApplicationSettings");
    }

    @Test
    void wrongPasswordsAndUnknownUsersShouldGetTheSame401() {
        var wrongPassword = login("root", "not the password");
        var unknownUser = login("nobody", PASSWORD);

        for (var response : new ResponseEntity<?>[]{wrongPassword, unknownUser}) {
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        }
        assertThat(json(wrongPassword).get("code").asText()).isEqualTo("BAD_CREDENTIALS");
        assertThat(json(wrongPassword).get("detail")).isEqualTo(json(unknownUser).get("detail"));
    }

    @Test
    void usersWithoutALocalPasswordShouldNotSignIn() {
        get("/api/v1/me", bearer("editor"));

        assertThat(login("editor", "editor").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(login("guest", "").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void credentialsInTheQueryStringShouldBeRefused() {
        var response = rest.postForEntity("/login?username=root&password=" + PASSWORD.replace(' ', '+'),
                                          new HttpEntity<>(new LinkedMultiValueMap<String, String>(), formHeaders()),
                                          String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).doesNotContain("access_token");
    }

    /**
     * A name is locked after {@link LocalLogin#MAX_FAILURES} failures in a row, whether the user exists or not, and
     * then even the right password is refused.
     */
    @Test
    void repeatedFailuresShouldLockTheUserName() {
        userService.createAdministrator(UserId.getUserId("lockable"), "lockable@example.test", PASSWORD);

        for (var i = 0; i < LocalLogin.MAX_FAILURES; i++) {
            assertThat(login("lockable", "wrong password " + i).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(login("no-such-user", "wrong password " + i).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        var locked = login("lockable", PASSWORD);
        assertThat(locked.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(json(locked).get("code").asText()).isEqualTo("TOO_MANY_ATTEMPTS");
        assertThat(login("no-such-user", PASSWORD).getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(login("root", PASSWORD).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void keycloakTokensShouldStillWork() {
        assertThat(json(get("/api/v1/me", bearer("viewer"))).get("userId").asText()).isEqualTo("viewer");
    }
}
