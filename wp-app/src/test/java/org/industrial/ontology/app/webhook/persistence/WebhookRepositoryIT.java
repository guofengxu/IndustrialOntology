package org.industrial.ontology.app.webhook.persistence;

import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.webhook.ProjectWebhook;
import org.industrial.ontology.domain.webhook.ProjectWebhookEventType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookRepositoryIT {

    private static final ProjectId PROJECT = ProjectId.get("11111111-1111-4111-8111-111111111111");

    private static final ProjectId OTHER_PROJECT = ProjectId.get("22222222-2222-4222-8222-222222222222");

    private static MongoPersistenceTestContext context;

    private WebhookRepository repository;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.start();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void clear() {
        context.clear();
        repository = context.bean(WebhookRepository.class);
    }

    @Test
    void shouldReplaceAProjectsWebhooks() {
        var changed = new ProjectWebhook(PROJECT, "https://hooks.example.org/a",
                                         List.of(ProjectWebhookEventType.PROJECT_CHANGED));
        var silent = new ProjectWebhook(OTHER_PROJECT, "https://hooks.example.org/b", List.of());
        repository.addProjectWebhooks(List.of(changed, silent));

        assertThat(repository.getProjectWebhooks(PROJECT)).containsExactly(changed);
        assertThat(repository.getProjectWebhooks(PROJECT, ProjectWebhookEventType.PROJECT_CHANGED))
                .containsExactly(changed);
        assertThat(repository.getProjectWebhooks(OTHER_PROJECT, ProjectWebhookEventType.PROJECT_CHANGED)).isEmpty();

        repository.clearProjectWebhooks(PROJECT);
        repository.addProjectWebhooks(List.of());
        assertThat(repository.getProjectWebhooks(PROJECT)).isEmpty();
        assertThat(repository.getProjectWebhooks(OTHER_PROJECT)).containsExactly(silent);
    }
}
