package org.industrial.ontology.api.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.industrial.ontology.app.error.WpException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@code POST /login} of the local fallback login (docs/01 §6), present only when
 * {@code webprotege.auth.local-login.enabled} is on. Takes the form fields {@code username} and {@code password} and
 * answers like an OAuth 2.0 token endpoint; a wrong user name or password gives 401 {@code BAD_CREDENTIALS}, and
 * {@link LocalLogin#MAX_FAILURES} failures in a row lock the user name for a while (429). The credentials must be in
 * the body: in the query string they would end up in access logs.
 */
@RestController
@ConditionalOnProperty(prefix = "webprotege.auth.local-login", name = "enabled", havingValue = "true")
@Tag(name = "Authentication")
public class LocalLoginController {

    private final LocalLogin localLogin;

    LocalLoginController(LocalLogin localLogin) {
        this.localLogin = checkNotNull(localLogin);
    }

    @Operation(summary = "Local fallback login: exchanges a local user name and password for a bearer token")
    @PostMapping(path = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<TokenResponse> login(@RequestParam String username,
                                               @RequestParam String password,
                                               HttpServletRequest request) {
        // Request parameters include the query string, which access logs record.
        if (request.getQueryString() != null) {
            throw WpException.invalidRequest("Send the user name and password in the request body only");
        }
        var token = localLogin.login(username, password);
        return ResponseEntity.ok()
                             .cacheControl(CacheControl.noStore())
                             .body(new TokenResponse(token.accessToken(), "Bearer",
                                                     token.expiresIn().toSeconds()));
    }

    /**
     * The fields of an OAuth 2.0 token response (RFC 6749 §5.1).
     */
    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("token_type") String tokenType,
                                @JsonProperty("expires_in") long expiresIn) {
    }
}
