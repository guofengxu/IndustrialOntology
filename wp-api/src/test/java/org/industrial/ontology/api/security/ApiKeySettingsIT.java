package org.industrial.ontology.api.security;

import org.industrial.ontology.api.ApiIntegrationTest;
import org.industrial.ontology.app.apikey.ApiKeyService;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code webprotege.auth.api-key.allow-query-parameter: false} (07 6-6): download links lose {@code ?apiKey=}, the
 * header keeps working.
 */
@TestPropertySource(properties = "webprotege.auth.api-key.allow-query-parameter=false")
class ApiKeySettingsIT extends ApiIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Test
    void downloadsShouldNotTakeTheKeyFromTheQuery() {
        var key = apiKeyService.generateApiKeyForUser(UserId.getUserId("viewer"), "no query keys").apiKey();
        var download = "/download?project=" + createProject("viewer", "No query keys").getId();

        assertThat(get(download + "&apiKey=" + key.getKey(), null).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(get(download, apiKey(key)).getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
