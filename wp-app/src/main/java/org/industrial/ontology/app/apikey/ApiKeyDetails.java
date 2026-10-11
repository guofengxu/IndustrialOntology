package org.industrial.ontology.app.apikey;

import org.industrial.ontology.domain.core.ApiKeyId;

import javax.annotation.Nonnull;
import java.time.Instant;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * What is known about a stored API key, without the key itself; the legacy {@code ApiKeyInfo} with an
 * {@link Instant} instead of epoch milliseconds.
 */
public record ApiKeyDetails(@Nonnull ApiKeyId apiKeyId, @Nonnull String purpose, @Nonnull Instant createdAt) {

    public ApiKeyDetails {
        checkNotNull(apiKeyId);
        checkNotNull(purpose);
        checkNotNull(createdAt);
    }
}
