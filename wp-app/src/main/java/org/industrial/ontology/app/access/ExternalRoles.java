package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.RoleId;
import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Application roles that the identity provider grants a user for the current request, on top of the stored role
 * assignments; they are never written to {@code RoleAssignments}.
 * <p>
 * docs/01 §6: a Keycloak token whose realm roles contain {@code webprotege.auth.admin-realm-role} makes its user a
 * {@code SystemAdmin} for that request. wp-api implements this from the security context; wp-app only asks.
 * {@link MongoAccessManager} adds these roles on the application resource, the resource that application roles
 * belong to, so they show up in permission checks and in the user's application actions alike.
 */
@FunctionalInterface
public interface ExternalRoles {

    /** No external roles: the command line, tests, and requests without an identity provider. */
    ExternalRoles NONE = userId -> Set.of();

    /**
     * The application roles granted to the user. Only the user of the current request can have any; for every other
     * user the result is empty.
     */
    @Nonnull
    Set<RoleId> getApplicationRoles(@Nonnull UserId userId);
}
