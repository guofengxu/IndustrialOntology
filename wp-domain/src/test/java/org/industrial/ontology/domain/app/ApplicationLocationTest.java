package org.industrial.ontology.domain.app;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.app.ApplicationLocation_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicationLocationTest {

    private ApplicationLocation applicationLocation;

    private String scheme = "The scheme";

    private String host = "The host";

    private String path = "The path";

    private int port = 1;

    @BeforeEach
    public void setUp() {
        applicationLocation = new ApplicationLocation(scheme, host, path, port);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_scheme_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ApplicationLocation(null, host, path, port);
        });
    }

    @Test
    public void shouldReturnSupplied_scheme() {
        assertThat(applicationLocation.getScheme(), is(this.scheme));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_host_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ApplicationLocation(scheme, null, path, port);
        });
    }

    @Test
    public void shouldReturnSupplied_host() {
        assertThat(applicationLocation.getHost(), is(this.host));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_path_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ApplicationLocation(scheme, host, null, port);
        });
    }

    @Test
    public void shouldReturnSupplied_path() {
        assertThat(applicationLocation.getPath(), is(this.path));
    }

    @Test
    public void shouldReturnSupplied_port() {
        assertThat(applicationLocation.getPort(), is(this.port));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(applicationLocation, is(applicationLocation));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(applicationLocation.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(applicationLocation, is(new ApplicationLocation(scheme, host, path, port)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_scheme() {
        assertThat(applicationLocation, is(not(new ApplicationLocation("String-d76cc1f8-72a8-4add-8d5f-05f13e927dde", host, path, port))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_host() {
        assertThat(applicationLocation, is(not(new ApplicationLocation(scheme, "String-023e1e8e-68c7-4cd7-aa19-6a4151fee5ce", path, port))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_path() {
        assertThat(applicationLocation, is(not(new ApplicationLocation(scheme, host, "String-ff99fbd1-4923-4b25-912c-c5cc16854b85", port))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_port() {
        assertThat(applicationLocation, is(not(new ApplicationLocation(scheme, host, path, 2))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(applicationLocation.hashCode(), is(new ApplicationLocation(scheme, host, path, port).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(applicationLocation.toString(), Matchers.startsWith("ApplicationLocation"));
    }
}
