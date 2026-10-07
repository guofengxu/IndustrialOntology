package org.industrial.ontology.domain.core;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.api.ApiKeyId_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApiKeyIdTest {

    private String id = "12345678-1234-1234-1234-123456789abc";

    private ApiKeyId apiKeyId;

    @BeforeEach
    public void setUp() {
        apiKeyId = ApiKeyId.valueOf(id);
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(apiKeyId, is(apiKeyId));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(apiKeyId.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(apiKeyId, is(ApiKeyId.valueOf(id)));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(apiKeyId.hashCode(), is(ApiKeyId.valueOf(id).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(apiKeyId.toString(), Matchers.startsWith("ApiKeyId"));
    }

    @Test
    public void should_getId() {
        assertThat(apiKeyId.getId(), is(id));
    }
}
