package org.industrial.ontology.api.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;

/**
 * RFC 7807 problem details with the stable {@code code} member of docs/02 §1, for the advice and for the security
 * chain, which answers before any controller runs.
 */
public final class Problems {

    /** The member that holds the stable error code. */
    public static final String CODE = "code";

    /** No or invalid credentials. */
    public static final String UNAUTHENTICATED = "UNAUTHENTICATED";

    /** A wrong user name or password at the local login. */
    public static final String BAD_CREDENTIALS = "BAD_CREDENTIALS";

    /** Too many failed local logins for the user name. */
    public static final String TOO_MANY_ATTEMPTS = "TOO_MANY_ATTEMPTS";

    /** Authentication failed on the server side, for example because the database is down. */
    public static final String AUTHENTICATION_UNAVAILABLE = "AUTHENTICATION_UNAVAILABLE";

    private Problems() {
    }

    @Nonnull
    public static ProblemDetail problem(@Nonnull HttpStatus status,
                                        @Nonnull String code,
                                        @Nullable String detail,
                                        @Nullable HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty(CODE, code);
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        return problem;
    }

    /**
     * Writes the problem as the response body, keeping the status and headers already set.
     */
    public static void write(@Nonnull HttpServletResponse response,
                             @Nonnull ProblemDetail problem,
                             @Nonnull ObjectMapper objectMapper) throws IOException {
        response.setStatus(problem.getStatus());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
