package org.industrial.ontology.api.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The local fallback login (docs/01 §6, 07 6-4), for development and for the first administrator, who has a local
 * password from {@code wp-cli create-admin --password}.
 * <p>
 * docs/01 asks for a {@code /login} form with BCrypt, and also for stateless authentication without cookies (07 6-5);
 * a session-based form login cannot be both. So {@code POST /login} takes the form fields, checks the password
 * against {@code Users.localPasswordHash}, and answers with a bearer token that this server signs. The client sends it
 * like a Keycloak token, and the same resource server chain verifies it (issuer {@value #ISSUER}).
 * <p>
 * The signing key is generated at startup and kept in memory: tokens end with a restart and are valid on this
 * instance only, which is enough for a fallback. Unknown users and wrong passwords give the same answer, after the
 * same BCrypt work ({@link DaoAuthenticationProvider}).
 * <p>
 * Guessing is limited per user name: after {@value #MAX_FAILURES} failures in a row the name is locked until
 * {@link #LOCKOUT} has passed since the last failure. Unknown names are counted too, so a lockout says nothing about
 * whether the user exists. The count is in memory, per instance, like the signing key.
 */
final class LocalLogin {

    static final String ISSUER = "urn:industrial-ontology:local-login";

    static final int MAX_FAILURES = 5;

    static final Duration LOCKOUT = Duration.ofMinutes(15);

    private static final Logger logger = LoggerFactory.getLogger(LocalLogin.class);

    private final DaoAuthenticationProvider passwords;

    /**
     * Failures in a row per user name (lower case); each failure restarts the lockout period.
     */
    private final Cache<String, Integer> failures = Caffeine.newBuilder()
                                                            .expireAfterWrite(LOCKOUT)
                                                            .maximumSize(10_000)
                                                            .build();

    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    private final Duration tokenTtl;

    private final Clock clock;

    LocalLogin(UserService userService, PasswordEncoder passwordEncoder, Duration tokenTtl, Clock clock) {
        checkNotNull(userService);
        this.passwords = new DaoAuthenticationProvider(userName -> {
            var hash = userService.findLocalPasswordHash(UserId.getUserId(userName))
                                  .orElseThrow(() -> new UsernameNotFoundException("No local password"));
            return User.withUsername(userName).password(hash).authorities(List.of()).build();
        });
        this.passwords.setPasswordEncoder(checkNotNull(passwordEncoder));
        this.tokenTtl = checkNotNull(tokenTtl);
        this.clock = checkNotNull(clock);
        var key = generateKey();
        this.encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
        try {
            var nimbusDecoder = NimbusJwtDecoder.withPublicKey(key.toRSAPublicKey()).build();
            nimbusDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(ISSUER));
            this.decoder = nimbusDecoder;
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot read the local login key", e);
        }
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Cannot generate the local login key", e);
        }
    }

    /**
     * Checks the password and issues a token for the user.
     *
     * @throws AuthenticationException if the user has no local password or the password is wrong
     */
    IssuedToken login(String userName, String password) {
        var key = userName.toLowerCase(Locale.ROOT);
        var failed = failures.getIfPresent(key);
        if (failed != null && failed >= MAX_FAILURES) {
            logger.warn("Local login for '{}' refused: locked after {} failures", userName, failed);
            throw new LockedException("Too many failed attempts");
        }
        Authentication authentication;
        try {
            authentication = passwords.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(userName,
                                                                                                       password));
        } catch (BadCredentialsException e) {
            var count = failures.asMap().merge(key, 1, Integer::sum);
            logger.warn("Failed local login for '{}' ({} in a row)", userName, count);
            throw e;
        }
        failures.invalidate(key);
        logger.info("Local login for '{}'", authentication.getName());
        var now = clock.instant();
        var claims = JwtClaimsSet.builder()
                                 .issuer(ISSUER)
                                 .subject(authentication.getName())
                                 .claim(UserJwtAuthenticationConverter.USER_NAME_CLAIM, authentication.getName())
                                 .issuedAt(now)
                                 .expiresAt(now.plus(tokenTtl))
                                 .id(UUID.randomUUID().toString())
                                 .build();
        var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        var token = encoder.encode(JwtEncoderParameters.from(header, claims));
        return new IssuedToken(token.getTokenValue(), tokenTtl);
    }

    /**
     * Verifies the tokens that {@link #login} issued.
     */
    JwtDecoder decoder() {
        return decoder;
    }

    record IssuedToken(String accessToken, Duration expiresIn) {
    }
}
