package org.industrial.ontology.domain.projectsettings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.projectsettings.WebhookSettings}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 8 Jun 2017
 */
public record WebhookSettings(@JsonProperty("webhookSettings") @Nonnull ImmutableList<WebhookSetting> webhookSettings) {

    public WebhookSettings {
        java.util.Objects.requireNonNull(webhookSettings, "Null webhookSettings");
    }

    @Nonnull
    @JsonCreator
    public static WebhookSettings get(@Nonnull @JsonProperty("webhookSettings") List<WebhookSetting> webhookSettings) {
        return new WebhookSettings(ImmutableList.copyOf(webhookSettings));
    }

    @JsonProperty("webhookSettings")
    @Nonnull
    public ImmutableList<WebhookSetting> getWebhookSettings() {
        return webhookSettings;
    }
}
