package org.industrial.ontology.domain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.EmailAddress_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailAddressTest {

    private EmailAddress emailAddress;

    private String address = "x@y.com";

    @BeforeEach
    public void setUp() {
        emailAddress = new EmailAddress(address);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_address_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new EmailAddress(null);
        });
    }

    @Test
    public void shouldReturnSupplied_address() {
        assertThat(emailAddress.getEmailAddress(), is(this.address));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(emailAddress, is(emailAddress));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(emailAddress.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(emailAddress, is(new EmailAddress(address)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_address() {
        assertThat(emailAddress, is(not(new EmailAddress("String-de67417b-eb48-4ebc-a141-08e715a54719"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(emailAddress.hashCode(), is(new EmailAddress(address).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(emailAddress.toString(), startsWith("EmailAddress"));
    }

    @Test
    public void shouldReturn_true_For_isEmpty() {
        EmailAddress emailAddress = new EmailAddress("");
        assertThat(emailAddress.isEmpty(), is(true));
    }

    @Test
    public void shouldReturn_false_For_isEmpty() {
        assertThat(emailAddress.isEmpty(), is(false));
    }

    @Test
    public void should_getEmailAddress() {
        assertThat(emailAddress.getEmailAddress(), is(address));
    }
}
