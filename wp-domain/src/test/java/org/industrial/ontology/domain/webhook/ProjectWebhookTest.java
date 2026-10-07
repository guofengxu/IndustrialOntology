package org.industrial.ontology.domain.webhook;

import org.industrial.ontology.domain.core.ProjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.webhook.ProjectWebhook_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectWebhookTest {

    private ProjectWebhook projectWebhook;

    @Mock
    private ProjectId projectId;

    private String payloadUrl = "The payloadUrl";

    @Mock
    private List<ProjectWebhookEventType> subscribedToEvents = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        subscribedToEvents = Collections.singletonList(ProjectWebhookEventType.PROJECT_CHANGED);
        projectWebhook = new ProjectWebhook(projectId, payloadUrl, subscribedToEvents);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_projectId_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectWebhook(null, payloadUrl, subscribedToEvents);
        });
    }

    @Test
    public void shouldReturnSupplied_projectId() {
        assertThat(projectWebhook.getProjectId(), is(this.projectId));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_payloadUrl_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectWebhook(projectId, null, subscribedToEvents);
        });
    }

    @Test
    public void shouldReturnSupplied_payloadUrl() {
        assertThat(projectWebhook.getPayloadUrl(), is(this.payloadUrl));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void shouldThrowNullPointerExceptionIf_subscribedToEvents_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new ProjectWebhook(projectId, payloadUrl, null);
        });
    }

    @Test
    public void shouldReturnSupplied_subscribedToEvents() {
        assertThat(projectWebhook.getSubscribedToEvents(), is(this.subscribedToEvents));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(projectWebhook, is(projectWebhook));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(projectWebhook.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(projectWebhook, is(new ProjectWebhook(projectId, payloadUrl, subscribedToEvents)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_projectId() {
        assertThat(projectWebhook, is(not(new ProjectWebhook(mock(ProjectId.class), payloadUrl, subscribedToEvents))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_payloadUrl() {
        assertThat(projectWebhook, is(not(new ProjectWebhook(projectId, "String-90554065-a9cf-485e-86b5-725b38241203", subscribedToEvents))));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_subscribedToEvents() {
        assertThat(projectWebhook, is(not(new ProjectWebhook(projectId, payloadUrl, new ArrayList<>()))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(projectWebhook.hashCode(), is(new ProjectWebhook(projectId, payloadUrl, subscribedToEvents).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(projectWebhook.toString(), startsWith("ProjectWebhook"));
    }
}
