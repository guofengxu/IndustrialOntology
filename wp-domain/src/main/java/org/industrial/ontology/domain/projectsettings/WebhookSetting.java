package org.industrial.ontology.domain.projectsettings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.projectsettings.WebhookSetting}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 8 Jun 2017
 */
public record WebhookSetting(@JsonProperty(WebhookSetting.PAYLOAD_URL) @Nonnull String payloadUrl, @JsonProperty(WebhookSetting.EVENT_TYPES) @Nonnull ImmutableSet<ProjectWebhookEventType> eventTypes) {

    public WebhookSetting {
        java.util.Objects.requireNonNull(payloadUrl, "Null payloadUrl");
        java.util.Objects.requireNonNull(eventTypes, "Null eventTypes");
    }

    public static final String PAYLOAD_URL = "payloadUrl";

    public static final String EVENT_TYPES = "eventTypes";

    @Nonnull
    public static WebhookSetting get(@Nonnull @JsonProperty(PAYLOAD_URL) String payloadUrl, @Nonnull @JsonProperty(EVENT_TYPES) Set<ProjectWebhookEventType> eventTypes) {
        return new WebhookSetting(payloadUrl, ImmutableSet.copyOf(eventTypes));
    }

    @JsonProperty(PAYLOAD_URL)
    @Nonnull
    public String getPayloadUrl() {
        return payloadUrl;
    }

    @JsonProperty(EVENT_TYPES)
    @Nonnull
    public ImmutableSet<ProjectWebhookEventType> getEventTypes() {
        return eventTypes;
    }
}
