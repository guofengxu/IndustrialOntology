package org.industrial.ontology.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.testcontainers.DockerClientFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The OpenID Connect provider of the security tests, started once per test JVM, with the realm that ships in
 * {@code deploy/keycloak/webprotege-realm.json} (on the test class path as {@value #REALM_FILE}).
 * <p>
 * {@code -Dwp.test.oidc} chooses it, like {@code -Dwp.test.mongo} for Mongo:
 * <ul>
 *     <li>{@code auto} (the default): a Keycloak container where Docker runs, otherwise the local issuer;</li>
 *     <li>{@code container}: always Keycloak, failing without Docker; CI uses this. The realm is imported as it is,
 *     and the test enables direct access grants on {@code webprotege-web} so that it can sign the dev users in with
 *     their passwords;</li>
 *     <li>{@code local}: an issuer in this JVM that serves the discovery document and the key set over HTTP and
 *     signs tokens shaped like Keycloak's (issuer, audience {@code webprotege-api}, {@code preferred_username},
 *     {@code name}, {@code email}, {@code realm_access.roles}) for the users of the same realm file. It is not
 *     Keycloak: the container run in CI is the one that counts.</li>
 * </ul>
 */
public final class OidcTestServer {

    public static final String MODE_PROPERTY = "wp.test.oidc";

    public static final String REALM = "webprotege";

    public static final String AUDIENCE = "webprotege-api";

    static final String REALM_FILE = "keycloak/webprotege-realm.json";

    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.4";

    private static final String WEB_CLIENT = "webprotege-web";

    private static final ObjectMapper JSON = new ObjectMapper();

    private static Provider provider;

    private OidcTestServer() {
    }

    /**
     * The issuer, for {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}.
     */
    public static synchronized String issuerUri() {
        return provider().issuerUri();
    }

    /**
     * An access token for a user of the realm, signed in with the password of the realm file.
     */
    public static synchronized String accessToken(String userName) {
        return provider().accessToken(realmUser(userName));
    }

    /**
     * What the tests run against, for messages.
     */
    public static synchronized String backend() {
        return provider().description();
    }

    private static Provider provider() {
        if (provider == null) {
            var mode = System.getProperty(MODE_PROPERTY, "auto");
            provider = switch (mode) {
                case "container" -> new KeycloakProvider();
                case "local" -> new LocalProvider();
                case "auto" -> dockerAvailable() ? new KeycloakProvider() : new LocalProvider();
                default -> throw new IllegalArgumentException(MODE_PROPERTY + " must be auto, container or local, "
                                                                      + "not " + mode);
            };
        }
        return provider;
    }

    private static boolean dockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * A user of the realm file.
     */
    record RealmUser(String userName, String password, String email, String name, List<String> realmRoles) {
    }

    static RealmUser realmUser(String userName) {
        var realm = realm();
        for (var user : realm.get("users")) {
            if (user.get("username").asText().equals(userName)) {
                var password = user.get("credentials").get(0).get("value").asText();
                var name = user.get("firstName").asText() + " " + user.get("lastName").asText();
                var roles = new ArrayList<String>();
                if (user.has("realmRoles")) {
                    user.get("realmRoles").forEach(role -> roles.add(role.asText()));
                }
                return new RealmUser(userName, password, user.get("email").asText(), name, roles);
            }
        }
        throw new IllegalArgumentException("No user " + userName + " in " + REALM_FILE);
    }

    private static JsonNode realm() {
        try (InputStream in = OidcTestServer.class.getClassLoader().getResourceAsStream(REALM_FILE)) {
            return JSON.readTree(Objects.requireNonNull(in, REALM_FILE));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private interface Provider {

        String issuerUri();

        String accessToken(RealmUser user);

        String description();
    }

    /**
     * Keycloak with the realm file, signing users in with the resource owner password grant.
     */
    private static final class KeycloakProvider implements Provider {

        private final KeycloakContainer keycloak;

        private final HttpClient http = HttpClient.newHttpClient();

        @SuppressWarnings("resource")
        KeycloakProvider() {
            keycloak = new KeycloakContainer(KEYCLOAK_IMAGE).withRealmImportFile(REALM_FILE);
            keycloak.start();
            Runtime.getRuntime().addShutdownHook(new Thread(keycloak::stop, "keycloak-test-container-stop"));
            var clients = keycloak.getKeycloakAdminClient().realm(REALM).clients();
            var webClient = clients.findByClientId(WEB_CLIENT).get(0);
            webClient.setDirectAccessGrantsEnabled(true);
            clients.get(webClient.getId()).update(webClient);
        }

        @Override
        public String issuerUri() {
            return keycloak.getAuthServerUrl().replaceAll("/+$", "") + "/realms/" + REALM;
        }

        @Override
        public String accessToken(RealmUser user) {
            var form = Map.of("grant_type", "password",
                              "client_id", WEB_CLIENT,
                              "scope", "openid",
                              "username", user.userName(),
                              "password", user.password())
                          .entrySet()
                          .stream()
                          .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(),
                                                                                StandardCharsets.UTF_8))
                          .collect(Collectors.joining("&"));
            var request = HttpRequest.newBuilder(URI.create(issuerUri() + "/protocol/openid-connect/token"))
                                     .header("Content-Type", "application/x-www-form-urlencoded")
                                     .POST(HttpRequest.BodyPublishers.ofString(form))
                                     .build();
            try {
                var response = http.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    throw new IllegalStateException("Keycloak refused " + user.userName() + ": " + response.body());
                }
                return JSON.readTree(response.body()).get("access_token").asText();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }

        @Override
        public String description() {
            return KEYCLOAK_IMAGE + " container";
        }
    }

    /**
     * An issuer in this JVM: the discovery document and the key set over HTTP, tokens signed with its own key.
     */
    private static final class LocalProvider implements Provider {

        private final RSAKey key;

        private final String issuer;

        LocalProvider() {
            try {
                key = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString()).generate();
                var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
                issuer = "http://127.0.0.1:" + server.getAddress().getPort() + "/realms/" + REALM;
                var path = URI.create(issuer).getPath();
                server.createContext(path + "/.well-known/openid-configuration",
                                     exchange -> respond(exchange, discoveryDocument()));
                server.createContext(path + "/protocol/openid-connect/certs",
                                     exchange -> respond(exchange, new JWKSet(key.toPublicJWK()).toString()));
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0), "oidc-test-issuer-stop"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (JOSEException e) {
                throw new IllegalStateException(e);
            }
        }

        private String discoveryDocument() throws IOException {
            return JSON.writeValueAsString(Map.of(
                    "issuer", issuer,
                    "jwks_uri", issuer + "/protocol/openid-connect/certs",
                    "authorization_endpoint", issuer + "/protocol/openid-connect/auth",
                    "token_endpoint", issuer + "/protocol/openid-connect/token",
                    "response_types_supported", List.of("code"),
                    "subject_types_supported", List.of("public"),
                    "id_token_signing_alg_values_supported", List.of("RS256")));
        }

        private static void respond(HttpExchange exchange, String json) throws IOException {
            var body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        }

        @Override
        public String issuerUri() {
            return issuer;
        }

        @Override
        public String accessToken(RealmUser user) {
            var now = Instant.now();
            var roles = new ArrayList<>(List.of("default-roles-" + REALM, "offline_access", "uma_authorization"));
            roles.addAll(user.realmRoles());
            var claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject(UUID.nameUUIDFromBytes(user.userName().getBytes(StandardCharsets.UTF_8)).toString())
                    .audience(List.of(AUDIENCE, "account"))
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(Duration.ofMinutes(5))))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("typ", "Bearer")
                    .claim("azp", WEB_CLIENT)
                    .claim("preferred_username", user.userName())
                    .claim("name", user.name())
                    .claim("email", user.email())
                    .claim("email_verified", true)
                    .claim("realm_access", Map.of("roles", roles))
                    .build();
            return sign(claims, key);
        }

        @Override
        public String description() {
            return "local issuer " + issuer;
        }
    }

    /**
     * Signs the claims with RS256, as Keycloak does; also for tokens that the tests forge.
     */
    public static String sign(JWTClaimsSet claims, RSAKey key) {
        try {
            var header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).type(JOSEObjectType.JWT)
                                                                   .build();
            var jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }
}
