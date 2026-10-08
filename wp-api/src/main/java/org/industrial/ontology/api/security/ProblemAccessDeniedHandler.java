package org.industrial.ontology.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.industrial.ontology.api.error.Problems;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Answers an authenticated request that the security chain itself refuses: 403 with the code
 * {@value PermissionDeniedException#CODE}, as the services' own checks (docs/01 §6).
 */
final class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    ProblemAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = checkNotNull(objectMapper);
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        Problems.write(response, Problems.problem(HttpStatus.FORBIDDEN, PermissionDeniedException.CODE,
                                                  exception.getMessage(), request), objectMapper);
    }
}
