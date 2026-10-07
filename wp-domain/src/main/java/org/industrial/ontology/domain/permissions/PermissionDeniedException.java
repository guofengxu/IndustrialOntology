package org.industrial.ontology.domain.permissions;



import org.industrial.ontology.domain.app.UserInSession;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.permissions.PermissionDeniedException}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/06/2012
 * <p>
 *     An exception thrown in the case where a user tried to carry out some operation but did not have the appropriate
 *     permissions to do so.
 * </p>
 */
public class PermissionDeniedException extends RuntimeException {

    private UserInSession userInSession;

    private PermissionDeniedException() {
    }

    public PermissionDeniedException(@Nonnull String message, @Nonnull UserInSession userInSession) {
        super(message);
        this.userInSession = checkNotNull(userInSession);
    }

    @Nonnull
    public UserInSession getUserInSession() {
        return userInSession;
    }
}
