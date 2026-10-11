package org.industrial.ontology.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.app.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The security chain (docs/01 §6, stage S5): one stateless chain for {@code /api/v1}, the {@code /download} and
 * {@code /data/*} compatibility paths and the actuator (07 6-6).
 * <ul>
 *     <li>Bearer tokens (07 6-1, 6-2): Keycloak's, verified with Spring Boot's decoder for
 *     {@code spring.security.oauth2.resourceserver.jwt.issuer-uri} (and {@code audiences}), and those of the local
 *     login; the token's issuer picks the decoder.</li>
 *     <li>API keys (07 6-3): {@link ApiKeyAuthenticationFilter}, after the bearer filter, so a JWT wins.</li>
 *     <li>The local fallback login (07 6-4): {@code POST /login}, when {@code webprotege.auth.local-login.enabled}.</li>
 *     <li>No sessions, no CSRF protection, no cookies (07 6-5): every request carries its credentials in the
 *     {@code Authorization} header, which a cross-site request cannot set.</li>
 * </ul>
 * Open without credentials: the health and info endpoints, the OpenAPI document and the error page. Everything else
 * needs an authenticated user; what the user may do is decided by the services ({@code AccessManager.require}), not
 * here; the exception is that creating and revoking API keys needs a bearer token. Prometheus scrapes with an API key
 * ({@code authorization: {type: ApiKey, credentials: ...}}); for now any authenticated caller can read the metrics,
 * which S10 restricts.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AuthProperties.class)
public class SecurityConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

    private static final String API_KEYS = "/api/v1/me/api-keys";

    /**
     * Requests authenticated by a bearer token (Keycloak or the local login); not by an API key.
     */
    private static final AuthorizationManager<RequestAuthorizationContext> BEARER_TOKEN_ONLY =
            (authentication, context) -> new AuthorizationDecision(
                    authentication.get() instanceof JwtAuthenticationToken);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthProperties auth,
                                                   ApiKeyService apiKeyService,
                                                   JwtIssuerAuthenticationManagerResolver bearerTokens,
                                                   ObjectMapper objectMapper) throws Exception {
        var entryPoint = new ProblemAuthenticationEntryPoint(objectMapper);
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(requests -> {
                requests.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**").permitAll();
                if (auth.localLogin().enabled()) {
                    requests.requestMatchers(HttpMethod.POST, "/login").permitAll();
                }
                // A leaked API key must not be able to make more keys, or revoke the owner's.
                requests.requestMatchers(HttpMethod.POST, API_KEYS).access(BEARER_TOKEN_ONLY)
                        .requestMatchers(HttpMethod.DELETE, API_KEYS + "/**").access(BEARER_TOKEN_ONLY)
                        .anyRequest().authenticated();
            })
            .oauth2ResourceServer(resourceServer -> resourceServer.authenticationManagerResolver(bearerTokens)
                                                                  .authenticationEntryPoint(entryPoint))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint)
                                                       .accessDeniedHandler(
                                                               new ProblemAccessDeniedHandler(objectMapper)));
        if (auth.apiKey().enabled()) {
            http.addFilterAfter(new ApiKeyAuthenticationFilter(apiKeyService, auth.apiKey().allowQueryParameter(),
                                                               entryPoint),
                                BearerTokenAuthenticationFilter.class);
        }
        return http.build();
    }

    /**
     * Chooses the decoder of a bearer token by its (not yet verified) issuer: Spring Boot's Keycloak decoder for the
     * configured issuer, the local login's for its own tokens. A token of any other issuer is invalid.
     */
    @Bean
    public JwtIssuerAuthenticationManagerResolver bearerTokenAuthenticationManagerResolver(
            AuthProperties auth,
            UserService userService,
            OAuth2ResourceServerProperties resourceServer,
            ObjectProvider<JwtDecoder> issuerDecoder,
            ObjectProvider<LocalLogin> localLogin) {
        var converter = new UserJwtAuthenticationConverter(userService, auth.adminRealmRole());
        Map<String, AuthenticationManager> managers = new HashMap<>();
        var issuerUri = resourceServer.getJwt().getIssuerUri();
        var decoder = issuerDecoder.getIfAvailable();
        if (issuerUri != null && decoder != null) {
            managers.put(issuerUri, manager(decoder, converter));
        }
        localLogin.ifAvailable(local -> managers.put(LocalLogin.ISSUER, manager(local.decoder(), converter)));
        return new JwtIssuerAuthenticationManagerResolver(managers::get);
    }

    private static AuthenticationManager manager(JwtDecoder decoder, UserJwtAuthenticationConverter converter) {
        var provider = new JwtAuthenticationProvider(decoder);
        provider.setJwtAuthenticationConverter(converter);
        return provider::authenticate;
    }

    @Bean
    @ConditionalOnProperty(prefix = "webprotege.auth.local-login", name = "enabled", havingValue = "true")
    LocalLogin localLogin(UserService userService, PasswordEncoder passwordEncoder, AuthProperties auth) {
        logger.warn("The local fallback login is on (webprotege.auth.local-login.enabled): POST /login accepts local "
                            + "passwords. Use it for development or the first administrator only.");
        return new LocalLogin(userService, passwordEncoder, auth.localLogin().tokenTtl(), Clock.systemUTC());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CallerArgumentResolver());
    }
}
