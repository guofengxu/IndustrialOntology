package org.industrial.ontology.app.apikey;

import org.industrial.ontology.domain.core.ApiKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.api.ApiKeyParser}: reads the key of an
 * {@code Authorization: ApiKey <key>} header. The scheme is matched ignoring case and may be followed by any white
 * space, as in the legacy server, so existing clients keep working.
 */
public final class ApiKeyParser {

    private static final Pattern PATTERN = Pattern.compile("apikey\\s+(.+)", Pattern.CASE_INSENSITIVE);

    private ApiKeyParser() {
    }

    /**
     * The key of the header value, or empty if the value is {@code null} or not of the {@code ApiKey} scheme.
     */
    @Nonnull
    public static Optional<ApiKey> parseApiKey(@Nullable String headerValue) {
        if (headerValue == null) {
            return Optional.empty();
        }
        var matcher = PATTERN.matcher(headerValue.trim());
        if (matcher.matches()) {
            return Optional.of(ApiKey.valueOf(matcher.group(1)));
        }
        return Optional.empty();
    }
}
