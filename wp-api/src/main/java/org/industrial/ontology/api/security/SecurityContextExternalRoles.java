package org.industrial.ontology.api.security;

import org.industrial.ontology.app.access.ExternalRoles;
import org.industrial.ontology.domain.core.BuiltInRole;
import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * The {@code SystemAdmin} role of a Keycloak administrator (docs/01 §6, 07 6-2), read from the security context of
 * the current request: it applies to the user of the request only, and only while the request runs. Work handed to
 * other threads does not see it.
 */
@Component
public class SecurityContextExternalRoles implements ExternalRoles {

    @Nonnull
    @Override
    public Set<RoleId> getApplicationRoles(@Nonnull UserId userId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !userId.getUserName().equals(authentication.getName())) {
            return Set.of();
        }
        var systemAdmin = authentication.getAuthorities()
                                        .stream()
                                        .anyMatch(authority -> UserJwtAuthenticationConverter.SYSTEM_ADMIN_AUTHORITY
                                                .equals(authority.getAuthority()));
        return systemAdmin ? Set.of(BuiltInRole.SYSTEM_ADMIN.getRoleId()) : Set.of();
    }
}
