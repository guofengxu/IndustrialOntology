package org.industrial.ontology.app.apikey;

import org.industrial.ontology.domain.core.ApiKey;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Converted from the legacy {@code ApiKeyParser_TestCase}.
 */
class ApiKeyParserTest {

    private final ApiKey expectedApiKey = ApiKey.valueOf("test key");

    @Test
    void shouldReturnEmptyForNullValue() {
        assertThat(ApiKeyParser.parseApiKey(null)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForMissingApiPrefix() {
        assertThat(ApiKeyParser.parseApiKey("test key")).isEmpty();
    }

    @Test
    void shouldReturnEmptyForMissingApiKey() {
        assertThat(ApiKeyParser.parseApiKey("apikey")).isEmpty();
    }

    @Test
    void shouldReturnEmptyForABearerToken() {
        assertThat(ApiKeyParser.parseApiKey("Bearer eyJhbGciOiJSUzI1NiJ9.e30.c2ln")).isEmpty();
    }

    @Test
    void shouldParseApiKey() {
        assertThat(ApiKeyParser.parseApiKey("apikey test key")).contains(expectedApiKey);
    }

    @Test
    void shouldParseApiKeyCaseInsensitive() {
        assertThat(ApiKeyParser.parseApiKey("ApiKey test key")).contains(expectedApiKey);
    }

    @Test
    void shouldParseApiKeyWithMultiSpaceSeparator() {
        assertThat(ApiKeyParser.parseApiKey("apikey   test key")).contains(expectedApiKey);
    }

    @Test
    void shouldParseApiKeyWithTrailingSpace() {
        assertThat(ApiKeyParser.parseApiKey("apikey test key ")).contains(expectedApiKey);
    }
}
