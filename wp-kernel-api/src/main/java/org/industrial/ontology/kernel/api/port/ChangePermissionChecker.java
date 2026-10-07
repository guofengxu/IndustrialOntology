package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.EntityType;

import javax.annotation.Nonnull;

/**
 * Checks whether a user may create entities of a given type while their changes are applied.
 * <p>
 * The legacy {@code ChangeManager} called {@code AccessManager} itself. Permissions now live in wp-app
 * (docs/01 §5.1): the application service requires {@code EDIT_ONTOLOGY} before it calls
 * {@code ChangeManager.applyChanges}, but which entities a change list mints is only known once the changes have
 * been generated under the project lock, so the kernel calls back here for the matching {@code CREATE_*} action.
 */
@FunctionalInterface
public interface ChangePermissionChecker {

    /** Accepts every request; for tests and for system-initiated changes. */
    ChangePermissionChecker ALLOW_ALL = (userId, entityType) -> {
    };

    /**
     * @throws RuntimeException (in wp-app a {@code PermissionDeniedException}) if the user may not create
     *                          entities of {@code entityType}
     */
    void checkCreatePermission(@Nonnull UserId userId, @Nonnull EntityType<?> entityType);
}
