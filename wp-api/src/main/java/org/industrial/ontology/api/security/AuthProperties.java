package org.industrial.ontology.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * {@code webprotege.auth.*} (docs/00 §8, docs/01 §6).
 *
 * @param apiKey         {@code Authorization: ApiKey <key>} authentication
 * @param localLogin     the local fallback login
 * @param adminRealmRole the Keycloak realm role whose users are {@code SystemAdmin} for the request
 */
@ConfigurationProperties("webprotege.auth")
public record AuthProperties(@DefaultValue ApiKey apiKey,
                             @DefaultValue LocalLogin localLogin,
                             @DefaultValue("webprotege-admin") String adminRealmRole) {

    /**
     * @param enabled              whether API keys authenticate requests
     * @param allowQueryParameter  whether {@code GET /download} also takes the key as {@code ?apiKey=}, for download
     *                             links that cannot carry a header; no other path does
     */
    public record ApiKey(@DefaultValue("true") boolean enabled,
                         @DefaultValue("true") boolean allowQueryParameter) {
    }

    /**
     * @param enabled  whether {@code POST /login} issues tokens for users with a local password; for development and
     *                 for the first administrator only
     * @param tokenTtl how long an issued token is valid
     */
    public record LocalLogin(@DefaultValue("false") boolean enabled,
                             @DefaultValue("PT8H") Duration tokenTtl) {
    }
}
