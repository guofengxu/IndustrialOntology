package org.industrial.ontology.app.apikey;

import org.industrial.ontology.domain.core.ApiKey;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A key that was just generated: the only time the key itself is available.
 */
public record GeneratedApiKey(@Nonnull ApiKey apiKey, @Nonnull ApiKeyDetails details) {

    public GeneratedApiKey {
        checkNotNull(apiKey);
        checkNotNull(details);
    }
}
