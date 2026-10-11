package org.industrial.ontology.app.error;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A failure that the API reports to its caller (docs/00 §4.5): a stable {@link #getCode() code} that clients can
 * branch on, and the HTTP status it maps to. wp-api renders it as RFC 7807 problem details; controllers do not catch
 * it. wp-app does not depend on Spring Web, so the status is a plain number.
 */
public class WpException extends RuntimeException {

    private final String code;

    private final int status;

    public WpException(@Nonnull String code, int status, @Nonnull String message) {
        super(checkNotNull(message));
        this.code = checkNotNull(code);
        this.status = status;
    }

    /**
     * A request that names something that does not exist, for example {@code API_KEY_NOT_FOUND}.
     */
    @Nonnull
    public static WpException notFound(@Nonnull String code, @Nonnull String message) {
        return new WpException(code, 404, message);
    }

    /**
     * A request whose content is not acceptable, such as a blank required value.
     */
    @Nonnull
    public static WpException invalidRequest(@Nonnull String message) {
        return new WpException("INVALID_REQUEST", 400, message);
    }

    /**
     * The stable error code, for example {@code PERMISSION_DENIED} (docs/02 §1).
     */
    @Nonnull
    public String getCode() {
        return code;
    }

    /**
     * The HTTP status of the response.
     */
    public int getStatus() {
        return status;
    }
}
