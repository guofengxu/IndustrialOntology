package org.industrial.ontology.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.industrial.ontology.api.error.Problems;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Answers a request without valid credentials: 401 with {@code WWW-Authenticate: Bearer} (with the RFC 6750 error of
 * an invalid token) and a problem body with the code {@value Problems#UNAUTHENTICATED} (docs/02 §1).
 */
final class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final BearerTokenAuthenticationEntryPoint bearerEntryPoint = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    ProblemAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = checkNotNull(objectMapper);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        bearerEntryPoint.commence(request, response, exception);
        var detail = exception instanceof InsufficientAuthenticationException
                ? "Sign in, or send an API key, to use this resource"
                : exception.getMessage();
        Problems.write(response, Problems.problem(HttpStatus.UNAUTHORIZED, Problems.UNAUTHENTICATED, detail, request),
                       objectMapper);
    }
}
