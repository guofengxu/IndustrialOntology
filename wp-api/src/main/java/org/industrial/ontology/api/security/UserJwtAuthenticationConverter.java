package org.industrial.ontology.api.security;

import org.industrial.ontology.app.user.UserService;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Turns a validated token into the authenticated user (docs/01 §6):
 * <ul>
 *     <li>the user id is the {@code preferred_username} claim, the Keycloak user name, which is also the
 *     {@code _id} of the user's {@code Users} record (07 6-1);</li>
 *     <li>a user seen for the first time gets a {@code Users} record with the {@code name} and {@code email} claims,
 *     the address only if it is not marked unverified (07 6-1);</li>
 *     <li>a user whose {@code realm_access.roles} contain {@code webprotege.auth.admin-realm-role} gets the authority
 *     {@value #SYSTEM_ADMIN_AUTHORITY}, which {@link SecurityContextExternalRoles} turns into {@code SystemAdmin} on
 *     the application for this request only; nothing is stored (07 6-2).</li>
 * </ul>
 * A token without a user name, or with one that the application takes for the guest user, is rejected as invalid.
 */
final class UserJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    static final String SYSTEM_ADMIN_AUTHORITY = "ROLE_SYSTEM_ADMIN";

    static final String USER_NAME_CLAIM = "preferred_username";

    private final UserService userService;

    private final String adminRealmRole;

    UserJwtAuthenticationConverter(UserService userService, String adminRealmRole) {
        this.userService = checkNotNull(userService);
        this.adminRealmRole = checkNotNull(adminRealmRole);
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        var userName = jwt.getClaimAsString(USER_NAME_CLAIM);
        if (userName == null || userName.isBlank()) {
            throw new InvalidBearerTokenException("The token has no " + USER_NAME_CLAIM);
        }
        var userId = UserId.getUserId(userName);
        if (userId.isGuest()) {
            throw new InvalidBearerTokenException("The user name " + userName + " is reserved for the guest user");
        }
        userService.registerIfAbsent(userId, jwt.getClaimAsString("name"), verifiedEmail(jwt));
        return new JwtAuthenticationToken(jwt, authorities(jwt), userName);
    }

    /**
     * The {@code email} claim, unless {@code email_verified} says that the address is not verified: notification
     * e-mails must not go to an address that someone merely typed in.
     */
    private static String verifiedEmail(Jwt jwt) {
        return Boolean.FALSE.equals(jwt.getClaimAsBoolean("email_verified")) ? null : jwt.getClaimAsString("email");
    }

    private Collection<GrantedAuthority> authorities(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.get("roles") instanceof Collection<?> roles
                && roles.contains(adminRealmRole)) {
            return List.of(new SimpleGrantedAuthority(SYSTEM_ADMIN_AUTHORITY));
        }
        return List.of();
    }
}
