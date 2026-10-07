package org.industrial.ontology.domain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.api.ApiKey_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApiKeyTest {

    private static final String LEXICAL_VALUE = "The Api Key";

    private ApiKey apiKey;

    @BeforeEach
    public void setUp() {
        apiKey = ApiKey.valueOf(LEXICAL_VALUE);
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(apiKey, is(apiKey));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(apiKey.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(apiKey, is(ApiKey.valueOf(LEXICAL_VALUE)));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(apiKey.hashCode(), is(ApiKey.valueOf(LEXICAL_VALUE).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(apiKey.toString(), startsWith("ApiKey"));
    }

    @Test
    public void should_getId() {
        assertThat(apiKey.getKey(), is(LEXICAL_VALUE));
    }
}
