package org.industrial.ontology.app.user;

import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.UserId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The signed-in user of a request, as {@code GET /api/v1/me} returns it (docs/02 §2); replaces the legacy
 * {@code UserInSession}.
 *
 * @param displayName        the real name, or the user name when the user has none
 * @param emailAddress       {@code null} when the address is not known
 * @param applicationActions the actions the user may perform on the application, sorted by id; the client shows the
 *                           administration pages by them
 */
public record CurrentUser(@Nonnull UserId userId,
                          @Nonnull String displayName,
                          @Nullable String emailAddress,
                          @Nonnull List<ActionId> applicationActions) {

    public CurrentUser {
        checkNotNull(userId);
        checkNotNull(displayName);
        applicationActions = List.copyOf(applicationActions);
    }
}
