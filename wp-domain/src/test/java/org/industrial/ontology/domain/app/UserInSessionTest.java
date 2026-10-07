package org.industrial.ontology.domain.app;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.ActionId;
import org.industrial.ontology.domain.core.UserDetails;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.app.UserInSession_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserInSessionTest {

    private UserInSession userInSession;

    @Mock
    private UserDetails userDetails;

    private Set<ActionId> allowedActions = ImmutableSet.of(mock(ActionId.class));

    @BeforeEach
    public void setUp() {
        userInSession = new UserInSession(userDetails, allowedActions);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_userDetails_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            new UserInSession(null, allowedActions);
        });
    }

    @Test
    public void shouldReturnSupplied_userDetails() {
        assertThat(userInSession.getUserDetails(), is(this.userDetails));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_allowedActions_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            new UserInSession(userDetails, null);
        });
    }

    @Test
    public void shouldReturnSupplied_allowedActions() {
        assertThat(userInSession.getAllowedApplicationActions(), is(this.allowedActions));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(userInSession, is(userInSession));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(userInSession.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(userInSession, is(new UserInSession(userDetails, allowedActions)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_userDetails() {
        assertThat(userInSession, is(not(new UserInSession(mock(UserDetails.class), allowedActions))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_allowedActions() {
        assertThat(userInSession, is(not(new UserInSession(userDetails, ImmutableSet.of(mock(ActionId.class))))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(userInSession.hashCode(), is(new UserInSession(userDetails, allowedActions).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(userInSession.toString(), Matchers.startsWith("UserInSession"));
    }
}
