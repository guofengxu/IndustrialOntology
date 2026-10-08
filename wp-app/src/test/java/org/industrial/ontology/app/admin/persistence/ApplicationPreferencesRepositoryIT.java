package org.industrial.ontology.app.admin.persistence;

import org.bson.Document;
import org.industrial.ontology.app.persistence.LegacyMongoSamples;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.app.ApplicationLocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPreferencesRepositoryIT {

    private static MongoPersistenceTestContext context;

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
    }

    @Test
    void shouldStoreTheLegacyDefaultsWhenThereAreNoPreferences() {
        var repository = new ApplicationPreferencesRepository(context.mongoTemplate());

        var preferences = repository.getApplicationPreferences();

        assertThat(preferences).isEqualTo(ApplicationPreferencesDocument.of(
                "WebProtégé", "", new ApplicationLocation("https", "", "", 443), Long.MAX_VALUE));
        assertThat(context.database().getCollection(ApplicationPreferencesDocument.COLLECTION).find().first())
                .containsEntry("_id", "Preferences");
    }

    @Test
    void shouldReadTheEmailAddressStoredBeforeTheFieldWasRenamed() {
        var legacy = LegacyMongoSamples.documents("ApplicationPreferences").get(0);
        legacy.remove("systemNotificationEmailAddress");
        legacy.append("adminEmailAddress", "old-admin@example.org");
        context.database().getCollection(ApplicationPreferencesDocument.COLLECTION).insertOne(legacy);
        var repository = new ApplicationPreferencesRepository(context.mongoTemplate());

        var preferences = repository.getApplicationPreferences();
        assertThat(preferences.systemNotificationEmailAddress()).isEqualTo("old-admin@example.org");

        repository.setApplicationPreferences(preferences);
        assertThat(context.database().getCollection(ApplicationPreferencesDocument.COLLECTION).find().first())
                .containsEntry("systemNotificationEmailAddress", "old-admin@example.org")
                .doesNotContainKey("adminEmailAddress");
    }

    @Test
    void shouldKeepThePreferencesItWasLastGiven() {
        var repository = new ApplicationPreferencesRepository(context.mongoTemplate());
        repository.getApplicationPreferences();
        var updated = ApplicationPreferencesDocument.of("Ontology", "ops@example.org",
                                                        new ApplicationLocation("http", "localhost", "", 8080), 10);

        repository.setApplicationPreferences(updated);

        assertThat(repository.getApplicationPreferences()).isEqualTo(updated);
        assertThat(new ApplicationPreferencesRepository(context.mongoTemplate()).getApplicationPreferences())
                .isEqualTo(updated);
        assertThat(context.database().getCollection(ApplicationPreferencesDocument.COLLECTION)
                          .countDocuments(new Document())).isEqualTo(1);
    }
}
