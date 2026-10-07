package org.industrial.ontology.domain.user;

import org.industrial.ontology.domain.core.ProjectId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.user.UserIdProjectIdKey_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserIdProjectIdKeyTest {

    private UserIdProjectIdKey userIdProjectIdKey;

    @Mock
    private UserId userId;

    @Mock
    private ProjectId projectId;

    @BeforeEach
    public void setUp() throws Exception {
        userIdProjectIdKey = new UserIdProjectIdKey(userId, projectId);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_userId_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            new UserIdProjectIdKey(null, projectId);
        });
    }

    @Test
    public void shouldReturnSupplied_userId() {
        assertThat(userIdProjectIdKey.getUserId(), is(this.userId));
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_projectId_IsNull() {
        assertThrows(java.lang.NullPointerException.class, () -> {
            new UserIdProjectIdKey(userId, null);
        });
    }

    @Test
    public void shouldReturnSupplied_projectId() {
        assertThat(userIdProjectIdKey.getProjectId(), is(this.projectId));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(userIdProjectIdKey, is(userIdProjectIdKey));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(userIdProjectIdKey.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(userIdProjectIdKey, is(new UserIdProjectIdKey(userId, projectId)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_userId() {
        assertThat(userIdProjectIdKey, is(Matchers.not(new UserIdProjectIdKey(mock(UserId.class), projectId))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_projectId() {
        assertThat(userIdProjectIdKey, is(Matchers.not(new UserIdProjectIdKey(userId, mock(ProjectId.class)))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(userIdProjectIdKey.hashCode(), is(new UserIdProjectIdKey(userId, projectId).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(userIdProjectIdKey.toString(), Matchers.startsWith("UserIdProjectIdKey"));
    }
}
