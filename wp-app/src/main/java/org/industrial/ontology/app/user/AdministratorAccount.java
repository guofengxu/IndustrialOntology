package org.industrial.ontology.app.user;

import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * What {@link UserService#createAdministrator} did, for the command line to report.
 *
 * @param created     whether the {@code Users} record was created, rather than updated
 * @param passwordSet whether the local password was set
 */
public record AdministratorAccount(@Nonnull UserId userId, boolean created, boolean passwordSet) {

    public AdministratorAccount {
        checkNotNull(userId);
    }
}
