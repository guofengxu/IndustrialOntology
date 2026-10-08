package org.industrial.ontology.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.industrial.ontology.app.apikey.ApiKeyParser;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.domain.core.ApiKey;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * API key authentication (docs/01 §6, 07 6-3): {@code Authorization: ApiKey <key>}, the header that the legacy
 * server's {@code AuthenticationFilter} read, so existing scripts and integrations keep working. The key is hashed
 * and looked up in {@code UserApiKeys}; on success the request is authenticated as the key's user with a
 * {@link UsernamePasswordAuthenticationToken}.
 * <p>
 * The filter runs after the bearer token filter: a request that a JWT has already authenticated is left alone, so
 * the JWT takes precedence. A key that belongs to nobody is answered with 401 at once rather than treated as
 * anonymous.
 * <p>
 * Download links cannot carry a header, so {@code GET /download} also takes the key as {@code ?apiKey=} when
 * {@code webprotege.auth.api-key.allow-query-parameter} is on (07 6-6). No other request does: a key in a URL ends up
 * in logs and browser histories.
 */
final class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    static final String QUERY_PARAMETER = "apiKey";

    static final String DOWNLOAD_PATH = "/download";

    private final ApiKeyService apiKeyService;

    private final boolean allowQueryParameter;

    private final AuthenticationEntryPoint entryPoint;

    private final SecurityContextRepository contextRepository = new RequestAttributeSecurityContextRepository();

    ApiKeyAuthenticationFilter(ApiKeyService apiKeyService,
                               boolean allowQueryParameter,
                               AuthenticationEntryPoint entryPoint) {
        this.apiKeyService = checkNotNull(apiKeyService);
        this.allowQueryParameter = allowQueryParameter;
        this.entryPoint = checkNotNull(entryPoint);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
        var apiKey = findApiKey(request);
        if (apiKey.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }
        var userId = apiKeyService.getUserIdForApiKey(apiKey.get()).filter(user -> !user.isGuest());
        if (userId.isEmpty()) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(request, response, new BadCredentialsException("Unknown API key"));
            return;
        }
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication(userId.get()));
        SecurityContextHolder.setContext(context);
        // As the bearer token filter does: an asynchronous response is dispatched again, and this filter does not run
        // then, so the context has to be found in the request.
        contextRepository.saveContext(context, request, response);
        chain.doFilter(request, response);
    }

    private static boolean isAuthenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Optional<ApiKey> findApiKey(HttpServletRequest request) {
        var fromHeader = ApiKeyParser.parseApiKey(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (fromHeader.isPresent() || !allowQueryParameter || !isDownload(request)) {
            return fromHeader;
        }
        return Optional.ofNullable(request.getParameter(QUERY_PARAMETER))
                       .filter(key -> !key.isBlank())
                       .map(ApiKey::valueOf);
    }

    private static boolean isDownload(HttpServletRequest request) {
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        return request.getRequestURI().substring(request.getContextPath().length()).equals(DOWNLOAD_PATH);
    }

    private static UsernamePasswordAuthenticationToken authentication(UserId userId) {
        return UsernamePasswordAuthenticationToken.authenticated(userId.getUserName(), null, List.of());
    }
}
