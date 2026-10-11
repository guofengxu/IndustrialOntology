package org.industrial.ontology.app.user;

import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A user as other users see it.
 *
 * @param displayName  the real name, or the user name when the user has none
 * @param emailAddress {@code null} when the address is not known
 */
public record UserProfile(@Nonnull UserId userId, @Nonnull String displayName, @Nullable String emailAddress) {

    public UserProfile {
        checkNotNull(userId);
        checkNotNull(displayName);
    }
}
