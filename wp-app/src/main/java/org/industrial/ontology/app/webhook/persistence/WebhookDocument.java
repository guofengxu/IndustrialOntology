package org.industrial.ontology.app.webhook.persistence;

import org.bson.types.ObjectId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.webhook.ProjectWebhook;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A document of the {@code ProjectWebhook} collection (Morphia named it after the class), as Morphia wrote the legacy
 * {@link ProjectWebhook}: {@code {_id: ObjectId, projectId, payloadUrl, subscribedToEvents: [eventType]}}.
 *
 * @param id server-generated; {@code null} for a webhook that has not been stored yet
 */
@Document(WebhookDocument.COLLECTION)
public record WebhookDocument(@Id @Nullable ObjectId id,
                              @Nonnull String projectId,
                              @Nonnull String payloadUrl,
                              @Nonnull List<ProjectWebhookEventType> subscribedToEvents) {

    public static final String COLLECTION = "ProjectWebhook";

    public static final String PROJECT_ID = "projectId";

    public static final String SUBSCRIBED_TO_EVENTS = "subscribedToEvents";

    public WebhookDocument {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(payloadUrl, "payloadUrl");
        subscribedToEvents = subscribedToEvents == null ? List.of() : List.copyOf(subscribedToEvents);
    }

    @Nonnull
    public static WebhookDocument of(@Nonnull ProjectWebhook webhook) {
        return new WebhookDocument(null, webhook.getProjectId().getId(), webhook.getPayloadUrl(),
                                   webhook.getSubscribedToEvents());
    }

    @Nonnull
    public ProjectWebhook toWebhook() {
        return new ProjectWebhook(ProjectId.get(projectId), payloadUrl, subscribedToEvents);
    }
}
