package org.industrial.ontology.app.webhook.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.webhook.ProjectWebhook;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.webhook.persistence.WebhookDocument.PROJECT_ID;
import static org.industrial.ontology.app.webhook.persistence.WebhookDocument.SUBSCRIBED_TO_EVENTS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code ProjectWebhook} collection; ported from the legacy {@code WebhookRepositoryImpl}. A project's webhooks
 * are replaced as a whole: {@link #clearProjectWebhooks} and then {@link #addProjectWebhooks}.
 */
public class WebhookRepository {

    private final MongoOperations mongo;

    public WebhookRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    public void clearProjectWebhooks(@Nonnull ProjectId projectId) {
        mongo.remove(byProjectId(projectId), WebhookDocument.class);
    }

    public void addProjectWebhooks(@Nonnull List<ProjectWebhook> webhooks) {
        if (!webhooks.isEmpty()) {
            mongo.insertAll(webhooks.stream().map(WebhookDocument::of).toList());
        }
    }

    @Nonnull
    public List<ProjectWebhook> getProjectWebhooks(@Nonnull ProjectId projectId) {
        return toWebhooks(byProjectId(projectId));
    }

    /**
     * The project's webhooks that subscribe to the event.
     */
    @Nonnull
    public List<ProjectWebhook> getProjectWebhooks(@Nonnull ProjectId projectId,
                                                   @Nonnull ProjectWebhookEventType event) {
        return toWebhooks(byProjectId(projectId).addCriteria(where(SUBSCRIBED_TO_EVENTS).is(event)));
    }

    private List<ProjectWebhook> toWebhooks(Query query) {
        return mongo.find(query, WebhookDocument.class).stream().map(WebhookDocument::toWebhook).toList();
    }

    private static Query byProjectId(ProjectId projectId) {
        return Query.query(where(PROJECT_ID).is(projectId.getId()));
    }
}
