package org.industrial.ontology.domain.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.UserDetails_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23/02/15
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserDetailsTest {

    private UserDetails userDetails;

    private UserDetails otherUserDetails;

    @Mock
    private UserId userId;

    private String displayName = "Display name";

    private Optional<String> emailAddress = Optional.of("Email Address");

    @BeforeEach
    public void setUp() throws Exception {
        userDetails = new UserDetails(userId, displayName, emailAddress);
        otherUserDetails = new UserDetails(userId, displayName, emailAddress);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_UserId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new UserDetails(null, displayName, emailAddress);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_DisplayName_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new UserDetails(userId, null, emailAddress);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_EmailAddress_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new UserDetails(userId, displayName, null);
        });
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(userDetails, is(equalTo(userDetails)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(userDetails, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(userDetails, is(equalTo(otherUserDetails)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(userDetails.hashCode(), is(otherUserDetails.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(userDetails.toString(), startsWith("UserDetails"));
    }

    @Test
    public void shouldReturnSuppliedUserId() {
        assertThat(userDetails.getUserId(), is(userId));
    }

    @Test
    public void shouldReturnSuppliedDisplayName() {
        assertThat(userDetails.getDisplayName(), is(displayName));
    }

    @Test
    public void shouldReturnSuppliedEmailAddress() {
        assertThat(userDetails.getEmailAddress(), is(emailAddress));
    }

    @Test
    public void shouldReturnGuestDetails() {
        assertThat(UserDetails.getGuestUserDetails().getUserId(), is(UserId.getGuest()));
    }

    @Test
    public void shouldBeEqualToGuestUser() {
        assertThat(UserDetails.getGuestUserDetails(), is(new UserDetails(UserId.getGuest(), "Guest", Optional.empty())));
    }
}
