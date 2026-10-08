package org.industrial.ontology.app.access;

import org.industrial.ontology.app.error.WpException;

import javax.annotation.Nonnull;

/**
 * The caller may not do what it asked (docs/01 §5.1): thrown by {@link AccessManager#require} at the entry of a
 * service method, and by the {@code CREATE_*} checks while changes are applied. wp-api answers 403 with the code
 * {@value #CODE}.
 * <p>
 * The legacy exception of the same name ({@code domain.permissions.PermissionDeniedException}) carried the GWT
 * session of the user; it is still declared by the kernel's {@code HasApplyChanges}, and wp-api maps it to 403 as
 * well.
 */
public class PermissionDeniedException extends WpException {

    public static final String CODE = "PERMISSION_DENIED";

    public PermissionDeniedException(@Nonnull String message) {
        super(CODE, 403, message);
    }
}
