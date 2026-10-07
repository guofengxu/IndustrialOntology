package org.industrial.ontology.domain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.api.ApiKeyInfo_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApiKeyInfoTest {

    private ApiKeyInfo apiKeyInfo;

    @Mock
    private ApiKeyId apiKeyId;

    private long createdAt = 1L;

    private String purpose = "The purpose";

    @BeforeEach
    public void setUp() {
        apiKeyInfo = new ApiKeyInfo(apiKeyId, createdAt, purpose);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_apiKeyId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ApiKeyInfo(null, createdAt, purpose);
        });
    }

    @Test
    public void shouldReturnSupplied_apiKeyId() {
        assertThat(apiKeyInfo.getApiKeyId(), is(this.apiKeyId));
    }

    @Test
    public void shouldReturnSupplied_createdAt() {
        assertThat(apiKeyInfo.getCreatedAt(), is(this.createdAt));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_purpose_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ApiKeyInfo(apiKeyId, createdAt, null);
        });
    }

    @Test
    public void shouldReturnSupplied_purpose() {
        assertThat(apiKeyInfo.getPurpose(), is(this.purpose));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(apiKeyInfo, is(apiKeyInfo));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(apiKeyInfo.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(apiKeyInfo, is(new ApiKeyInfo(apiKeyId, createdAt, purpose)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_apiKeyId() {
        assertThat(apiKeyInfo, is(not(new ApiKeyInfo(Mockito.mock(ApiKeyId.class), createdAt, purpose))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_createdAt() {
        assertThat(apiKeyInfo, is(not(new ApiKeyInfo(apiKeyId, 2L, purpose))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_purpose() {
        assertThat(apiKeyInfo, is(not(new ApiKeyInfo(apiKeyId, createdAt, "String-f303a24a-2b5d-472d-ae61-0fa43d97f70b"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(apiKeyInfo.hashCode(), is(new ApiKeyInfo(apiKeyId, createdAt, purpose).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(apiKeyInfo.toString(), startsWith("ApiKeyInfo"));
    }
}
