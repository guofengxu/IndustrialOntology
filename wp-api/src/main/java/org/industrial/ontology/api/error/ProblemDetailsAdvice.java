package org.industrial.ontology.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The one place where exceptions become responses (docs/00 §4.5): RFC 7807 problem details with a stable
 * {@code code} (docs/02 §1). Controllers do not catch exceptions.
 * <ul>
 *     <li>{@link WpException}: its status and code, for example 403 {@code PERMISSION_DENIED};</li>
 *     <li>the legacy kernel's {@code PermissionDeniedException}: 403 {@code PERMISSION_DENIED} as well;</li>
 *     <li>Spring MVC's own exceptions (unreadable body, missing parameter, unsupported method...): their status, with
 *     the status name as the code.</li>
 * </ul>
 */
@RestControllerAdvice
public class ProblemDetailsAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(WpException.class)
    public ResponseEntity<ProblemDetail> handle(WpException exception, HttpServletRequest request) {
        var status = HttpStatus.valueOf(exception.getStatus());
        return response(Problems.problem(status, exception.getCode(), exception.getMessage(), request));
    }

    @ExceptionHandler(org.industrial.ontology.domain.permissions.PermissionDeniedException.class)
    public ResponseEntity<ProblemDetail> handle(org.industrial.ontology.domain.permissions.PermissionDeniedException
                                                        exception, HttpServletRequest request) {
        return response(Problems.problem(HttpStatus.FORBIDDEN, PermissionDeniedException.CODE, exception.getMessage(),
                                         request));
    }

    /**
     * Method security, should a controller ever use it; the services' own checks throw
     * {@link PermissionDeniedException}.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handle(AccessDeniedException exception, HttpServletRequest request) {
        return response(Problems.problem(HttpStatus.FORBIDDEN, PermissionDeniedException.CODE, exception.getMessage(),
                                         request));
    }

    /**
     * A failed local login: unknown users and wrong passwords get the same answer.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handle(BadCredentialsException exception, HttpServletRequest request) {
        return response(Problems.problem(HttpStatus.UNAUTHORIZED, Problems.BAD_CREDENTIALS,
                                         "Wrong user name or password", request));
    }

    /**
     * Too many failed local logins for the user name.
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetail> handle(LockedException exception, HttpServletRequest request) {
        return response(Problems.problem(HttpStatus.TOO_MANY_REQUESTS, Problems.TOO_MANY_ATTEMPTS,
                                         "Too many failed attempts; try again later", request));
    }

    /**
     * Authentication could not be decided, for example because the database is down: not the caller's fault, so not
     * 401, and the cause stays in the log.
     */
    @ExceptionHandler(AuthenticationServiceException.class)
    public ResponseEntity<ProblemDetail> handle(AuthenticationServiceException exception, HttpServletRequest request) {
        logger.error("Authentication failed on the server side", exception);
        return response(Problems.problem(HttpStatus.SERVICE_UNAVAILABLE, Problems.AUTHENTICATION_UNAVAILABLE,
                                         "Authentication is unavailable; try again later", request));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handle(AuthenticationException exception, HttpServletRequest request) {
        return response(Problems.problem(HttpStatus.UNAUTHORIZED, Problems.UNAUTHENTICATED, "Authentication failed",
                                         request));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception,
                                                             Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        if (body instanceof ProblemDetail problem
                && (problem.getProperties() == null || !problem.getProperties().containsKey(Problems.CODE))) {
            var status = HttpStatus.resolve(statusCode.value());
            problem.setProperty(Problems.CODE, status == null ? String.valueOf(statusCode.value()) : status.name());
        }
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }

    private static ResponseEntity<ProblemDetail> response(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).body(problem);
    }
}
