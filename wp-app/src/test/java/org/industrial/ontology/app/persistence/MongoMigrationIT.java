package org.industrial.ontology.app.persistence;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * migrate-mongo on a legacy database (docs/01 §5.3): the 19 legacy samples with the {@code className} fields of old
 * Morphia versions, a Jackson document that has a {@code className} of its own, and two documents whose entity
 * cannot be read.
 */
class MongoMigrationIT {

    private static final String LEGACY_CLASS = "edu.stanford.bmir.protege.web.LegacyClass";

    private static final ObjectId JACKSON_ENTITY_WATCH = new ObjectId("6a0000000000000000000001");

    private static final ObjectId TAGS_WITHOUT_ENTITY = new ObjectId("6a0000000000000000000002");

    private MongoPersistenceTestContext context;

    private MongoMigration migration;

    @BeforeEach
    void importLegacyDatabase() {
        context = MongoPersistenceTestContext.start("webprotege.mongo.ensure-indexes=false");
        migration = context.bean(MongoMigration.class);
        for (var collection : LegacyCollection.values()) {
            var documents = LegacyMongoSamples.documents(collection.collectionName());
            documents.forEach(document -> addClassNames(document, collection));
            context.database().getCollection(collection.collectionName()).insertMany(documents);
        }
        // A portlet property that happens to be called className: Jackson data, which migrate-mongo leaves alone.
        context.database().getCollection("PerspectiveLayouts").updateOne(
                new Document("perspectiveId", "69df8fa8-4f84-499e-9341-28eb5085c40b"),
                new Document("$set", new Document("layout.children.0.node.properties.className", "portlet")));
        context.database().getCollection("Watches").insertOne(
                new Document("_id", JACKSON_ENTITY_WATCH)
                        .append("projectId", "2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81")
                        .append("userId", "carol")
                        .append("entity", new Document("type", "owl:Class").append("iri", "http://example.org/A"))
                        .append("type", "ENTITY"));
        context.database().getCollection("EntityTags").insertOne(
                new Document("_id", TAGS_WITHOUT_ENTITY)
                        .append("projectId", "2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81")
                        .append("tags", List.of()));
    }

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void dryRunShouldReportWithoutChangingAnything() {
        var before = snapshot();

        var report = migration.run(true);

        assertThat(report.dryRun()).isTrue();
        assertThat(report.classNameRemovals().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(sampleDocumentCount());
        assertThat(report.indexes()).extracting(MongoIndexes.Result::outcome)
                                    .containsOnly(MongoIndexes.Outcome.MISSING)
                                    .hasSize(16);
        assertThat(report.anomalies()).hasSize(2);
        assertThat(snapshot()).isEqualTo(before);
    }

    @Test
    void shouldRemoveClassNamesCreateIndexesAndReportUnreadableEntities() {
        var report = migration.run(false);

        assertThat(report.classNameRemovals()).containsEntry(LegacyCollection.ENTITY_DISCUSSION_THREADS, 2L)
                                              .containsEntry(LegacyCollection.PROJECT_DETAILS, 2L)
                                              .containsEntry(LegacyCollection.APPLICATION_PREFERENCES, 1L);
        assertThat(report.indexes()).extracting(MongoIndexes.Result::outcome)
                                    .containsOnly(MongoIndexes.Outcome.CREATED);
        assertThat(report.anomalies()).containsExactlyInAnyOrder(
                new MongoMigration.Anomaly(LegacyCollection.ENTITY_TAGS, TAGS_WITHOUT_ENTITY, "entity", "missing"),
                new MongoMigration.Anomaly(LegacyCollection.WATCHES, JACKSON_ENTITY_WATCH, "entity",
                                           "unknown entity type 'owl:Class'"));
        assertThat(report.hasProblems()).isTrue();

        // Everything is back to what the legacy code wrote, except for the documents added above.
        for (var collection : LegacyCollection.values()) {
            var stored = LegacyMongoSamples.stored(context.database(), collection.collectionName())
                                           .stream()
                                           .filter(json -> !json.contains(JACKSON_ENTITY_WATCH.toHexString()))
                                           .filter(json -> !json.contains(TAGS_WITHOUT_ENTITY.toHexString()))
                                           .toList();
            if (collection == LegacyCollection.PERSPECTIVE_LAYOUTS) {
                assertThat(stored.get(0)).contains("\"className\": \"portlet\"");
                continue;
            }
            assertThat(stored).as(collection.collectionName())
                              .containsExactlyElementsOf(LegacyMongoSamples.lines(collection.collectionName()));
        }
        // The Jackson type id inside the crud kit settings is data, not a Spring or Morphia type hint.
        assertThat(LegacyMongoSamples.stored(context.database(), "EntityCrudKitSettings").get(0))
                .contains("\"_class\": \"Uuid\"");
    }

    @Test
    void shouldChangeNothingWhenRunAgain() {
        migration.run(false);
        var afterFirstRun = snapshot();

        var report = migration.run(false);

        assertThat(report.classNameRemovals().values()).containsOnly(0L);
        assertThat(report.indexes()).extracting(MongoIndexes.Result::outcome)
                                    .containsOnly(MongoIndexes.Outcome.PRESENT);
        assertThat(report.anomalies()).hasSize(2);
        assertThat(snapshot()).isEqualTo(afterFirstRun);
    }

    @Test
    void shouldHaveNoProblemsOnceTheUnreadableDocumentsAreFixed() {
        context.database().getCollection("Watches").deleteOne(new Document("_id", JACKSON_ENTITY_WATCH));
        context.database().getCollection("EntityTags").deleteOne(new Document("_id", TAGS_WITHOUT_ENTITY));

        assertThat(migration.run(false).hasProblems()).isFalse();
    }

    /**
     * Adds {@code className} where old Morphia versions stored it: at the top level of every document, and in the
     * embedded objects of the collections Morphia wrote, but not in the entity, which went through a converter.
     */
    private static void addClassNames(Document document, LegacyCollection collection) {
        if (collection == LegacyCollection.PROJECT_ACCESS) {
            return;
        }
        document.put(MongoMigration.CLASS_NAME, LEGACY_CLASS);
        if (collection.storage() == LegacyCollection.Storage.MORPHIA) {
            document.forEach((field, value) -> {
                if (!field.equals("entity")) {
                    addEmbeddedClassNames(value);
                }
            });
        }
    }

    private static void addEmbeddedClassNames(Object value) {
        if (value instanceof Document embedded) {
            embedded.put(MongoMigration.CLASS_NAME, LEGACY_CLASS);
        } else if (value instanceof List<?> list) {
            list.forEach(MongoMigrationIT::addEmbeddedClassNames);
        }
    }

    private static long sampleDocumentCount() {
        return Stream.of(LegacyCollection.values())
                     .filter(collection -> collection != LegacyCollection.PROJECT_ACCESS)
                     .mapToLong(collection -> LegacyMongoSamples.lines(collection.collectionName()).size())
                     .sum();
    }

    private List<String> snapshot() {
        var snapshot = new ArrayList<String>();
        for (var name : context.database().listCollectionNames().into(new ArrayList<>()).stream().sorted().toList()) {
            snapshot.add(name);
            snapshot.addAll(LegacyMongoSamples.stored(context.database(), name));
            context.database().getCollection(name).listIndexes().forEach(index -> snapshot.add(index.toJson()));
        }
        return snapshot;
    }
}
