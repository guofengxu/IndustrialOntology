package org.industrial.ontology.app.persistence;

import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MongoIndexesIT {

    private MongoPersistenceTestContext context;

    private MongoIndexes indexes;

    @BeforeEach
    void startWithoutIndexes() {
        context = MongoPersistenceTestContext.start("webprotege.mongo.ensure-indexes=false");
        indexes = context.bean(MongoIndexes.class);
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void shouldCreateTheLegacyIndexesOnceAndThenFindThem() {
        assertThat(outcomes(indexes.ensureIndexes(true))).containsOnly(MongoIndexes.Outcome.MISSING).hasSize(16);
        assertThat(context.database().listCollectionNames().into(new ArrayList<>())).isEmpty();

        assertThat(outcomes(indexes.ensureIndexes(false))).containsOnly(MongoIndexes.Outcome.CREATED).hasSize(16);
        assertThat(outcomes(indexes.ensureIndexes(false))).containsOnly(MongoIndexes.Outcome.PRESENT).hasSize(16);

        var watches = indexesOf("Watches");
        assertThat(watches).anySatisfy(index -> {
            assertThat(index.getString("name")).isEqualTo("projectId_1_userId_1_entity_1");
            assertThat(index.getBoolean("unique")).isTrue();
        });
        assertThat(indexesOf("EntityDiscussionThreads")).extracting(index -> index.getString("name"))
                                                         .contains("projectId_1_entity_1_status_1",
                                                                   "comments._id_1");
    }

    /**
     * A legacy database already has most of the indexes, under Mongo's default names; they are not created again.
     */
    @Test
    void shouldKeepAnExistingLegacyIndex() {
        context.database().getCollection("Tags").createIndex(new Document("projectId", 1).append("label", 1),
                                                             new IndexOptions().unique(true));

        var tags = indexes.ensureIndexes(false)
                          .stream()
                          .filter(result -> result.collection() == LegacyCollection.TAGS)
                          .toList();

        assertThat(outcomes(tags)).containsExactly(MongoIndexes.Outcome.PRESENT);
        assertThat(indexesOf("Tags")).hasSize(2);
    }

    @Test
    void shouldReportButKeepAnIndexOnTheSameKeysWithAnotherUniqueness() {
        context.database().getCollection("Tags").createIndex(new Document("projectId", 1).append("label", 1));

        var tags = indexes.ensureIndexes(false)
                          .stream()
                          .filter(result -> result.collection() == LegacyCollection.TAGS)
                          .toList();

        assertThat(tags).singleElement().satisfies(result -> {
            assertThat(result.outcome()).isEqualTo(MongoIndexes.Outcome.FAILED);
            assertThat(result.problem()).contains("projectId_1_label_1").contains("unique=false");
        });
    }

    @Test
    void shouldReportAUniqueIndexThatTheDataViolates() {
        var tags = context.database().getCollection("Tags");
        tags.insertOne(new Document("_id", "t1").append("projectId", "p").append("label", "Same"));
        tags.insertOne(new Document("_id", "t2").append("projectId", "p").append("label", "Same"));

        var results = indexes.ensureIndexes(false);

        assertThat(results).filteredOn(result -> result.collection() == LegacyCollection.TAGS)
                           .singleElement()
                           .satisfies(result -> {
                               assertThat(result.outcome()).isEqualTo(MongoIndexes.Outcome.FAILED);
                               assertThat(result.problem()).isNotBlank();
                           });
        assertThat(results).filteredOn(result -> result.collection() != LegacyCollection.TAGS)
                           .extracting(MongoIndexes.Result::outcome)
                           .containsOnly(MongoIndexes.Outcome.CREATED);
    }

    private List<Document> indexesOf(String collection) {
        return context.database().getCollection(collection).listIndexes().into(new ArrayList<>());
    }

    private static List<MongoIndexes.Outcome> outcomes(List<MongoIndexes.Result> results) {
        return results.stream().map(MongoIndexes.Result::outcome).toList();
    }
}
