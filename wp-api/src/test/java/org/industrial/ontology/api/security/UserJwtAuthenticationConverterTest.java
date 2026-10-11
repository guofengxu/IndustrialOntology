package org.industrial.ontology.api.security;

import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * From a validated token to the user (07 6-1, 6-2).
 */
class UserJwtAuthenticationConverterTest {

    private final UserService userService = mock(UserService.class);

    private final UserJwtAuthenticationConverter converter = new UserJwtAuthenticationConverter(userService,
                                                                                                "webprotege-admin");

    private static Jwt jwt(Consumer<Map<String, Object>> claims) {
        return Jwt.withTokenValue("token")
                  .header("alg", "RS256")
                  .issuedAt(Instant.now())
                  .expiresAt(Instant.now().plusSeconds(300))
                  .claims(claims)
                  .build();
    }

    @Test
    void shouldNameTheUserByPreferredUsernameAndRegisterIt() {
        var authentication = converter.convert(jwt(claims -> {
            claims.put("preferred_username", "editor");
            claims.put("name", "Editor Dev");
            claims.put("email", "editor@example.test");
            claims.put("email_verified", true);
        }));

        assertThat(authentication.getName()).isEqualTo("editor");
        assertThat(authentication.getAuthorities()).isEmpty();
        verify(userService).registerIfAbsent(UserId.getUserId("editor"), "Editor Dev", "editor@example.test");
    }

    @Test
    void shouldNotKeepAnUnverifiedEmailAddress() {
        converter.convert(jwt(claims -> {
            claims.put("preferred_username", "editor");
            claims.put("email", "someone-else@example.test");
            claims.put("email_verified", false);
        }));

        verify(userService).registerIfAbsent(UserId.getUserId("editor"), null, null);
    }

    @Test
    void theAdminRealmRoleShouldGiveTheSystemAdminAuthority() {
        var admin = converter.convert(jwt(claims -> {
            claims.put("preferred_username", "admin");
            claims.put("realm_access", Map.of("roles", List.of("offline_access", "webprotege-admin")));
        }));
        var other = converter.convert(jwt(claims -> {
            claims.put("preferred_username", "viewer");
            claims.put("realm_access", Map.of("roles", List.of("offline_access")));
            claims.put("resource_access", Map.of("webprotege-web", Map.of("roles", List.of("webprotege-admin"))));
        }));

        assertThat(admin.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                                          .containsExactly(UserJwtAuthenticationConverter.SYSTEM_ADMIN_AUTHORITY);
        assertThat(other.getAuthorities()).isEmpty();
    }

    /**
     * {@code UserId.isGuest()} is {@code "guest".endsWith(name)}: such names would make the caller the guest user.
     */
    @Test
    void shouldRejectTokensWithoutAUserOrWithAGuestName() {
        for (var userName : new String[]{"guest", "est", "t"}) {
            assertThatThrownBy(() -> converter.convert(jwt(claims -> claims.put("preferred_username", userName))))
                    .as(userName)
                    .isInstanceOf(InvalidBearerTokenException.class);
        }
        assertThatThrownBy(() -> converter.convert(jwt(claims -> claims.put("sub", "no user name"))))
                .isInstanceOf(InvalidBearerTokenException.class);
        verify(userService, never()).registerIfAbsent(any(), any(), any());
    }
}
