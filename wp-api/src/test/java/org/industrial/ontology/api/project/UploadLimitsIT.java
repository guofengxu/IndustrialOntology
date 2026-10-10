package org.industrial.ontology.api.project;

import org.industrial.ontology.api.ApiIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An upload over {@code spring.servlet.multipart.max-file-size} gets the same answer as one over the application's
 * maximum upload size: 413 {@code UPLOAD_TOO_LARGE}.
 */
@TestPropertySource(properties = "spring.servlet.multipart.max-file-size=1KB")
class UploadLimitsIT extends ApiIntegrationTest {

    @Test
    void anUploadOverTheMultipartLimitShouldGet413() {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource(new byte[2048]) {
            @Override
            public String getFilename() {
                return "large.owl";
            }
        });
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, bearer("editor"));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var response = rest.exchange("/api/v1/uploads", HttpMethod.POST, new HttpEntity<>(body, headers),
                                     String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(json(response).get("code").asText()).isEqualTo("UPLOAD_TOO_LARGE");
    }
}
