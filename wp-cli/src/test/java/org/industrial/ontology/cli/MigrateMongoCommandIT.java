package org.industrial.ontology.cli;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.industrial.ontology.app.persistence.LegacyCollection;
import org.industrial.ontology.app.persistence.LegacyMongoSamples;
import org.industrial.ontology.app.persistence.MongoMigration;
import org.industrial.ontology.app.persistence.MongoTestServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code wp-cli migrate-mongo} end to end: the command-line application on a database holding the legacy samples, as
 * an old WebProtégé left them (with Morphia's {@code className}).
 */
@ExtendWith(OutputCaptureExtension.class)
class MigrateMongoCommandIT {

    private final String databaseName = MongoTestServer.uniqueDatabase();

    private MongoClient client;

    private MongoDatabase database;

    @BeforeEach
    void importLegacyDatabase() {
        client = MongoClients.create(MongoTestServer.uri(databaseName));
        database = client.getDatabase(databaseName);
        for (var collection : LegacyCollection.values()) {
            var documents = LegacyMongoSamples.documents(collection.collectionName());
            documents.forEach(document -> document.put(MongoMigration.CLASS_NAME, "edu.stanford.bmir.Legacy"));
            database.getCollection(collection.collectionName()).insertMany(documents);
        }
    }

    @AfterEach
    void dropDatabase() {
        database.drop();
        client.close();
    }

    @Test
    void dryRunShouldReportWhatItWouldDoAndChangeNothing(CapturedOutput output) {
        assertThat(run("migrate-mongo", "--dry-run")).isZero();

        assertThat(output).contains("dry run, nothing is changed")
                          .contains("  UserActivity: 2")
                          .contains("missing  Watches [projectId, userId, entity] unique")
                          .contains("0 of 16 present before this run")
                          .contains("Done.");
        assertThat(database.getCollection("UserActivity").countDocuments(new Document("className",
                                                                                       new Document("$exists", true))))
                .isEqualTo(2);
        assertThat(database.getCollection("Watches").listIndexes().into(new ArrayList<>())).hasSize(1);
    }

    @Test
    void shouldMigrateOnceAndThenHaveNothingLeftToDo(CapturedOutput output) {
        assertThat(run("migrate-mongo")).isZero();
        assertThat(output).contains("className removed from:")
                          .contains("created  EntityDiscussionThreads [comments._id] unique");
        for (var collection : LegacyCollection.values()) {
            assertThat(LegacyMongoSamples.stored(database, collection.collectionName()))
                    .as(collection.collectionName())
                    .containsExactlyElementsOf(LegacyMongoSamples.lines(collection.collectionName()));
        }

        assertThat(run("migrate-mongo")).isZero();
        assertThat(output.getOut().substring(output.getOut().lastIndexOf("migrate-mongo")))
                .contains("className removed from:" + System.lineSeparator() + "  none")
                .contains("16 of 16 present before this run");
    }

    @Test
    void shouldListUnreadableEntitiesAndExitWithProblemsFound(CapturedOutput output) {
        database.getCollection("Watches").insertOne(
                new Document("_id", "broken")
                        .append("projectId", "2a6d7a1e-5a3e-4c34-9f0b-3c4f5d6e7a81")
                        .append("userId", "carol")
                        .append("entity", new Document("type", "owl:Class").append("iri", "http://example.org/A"))
                        .append("type", "ENTITY"));

        assertThat(run("migrate-mongo")).isEqualTo(MigrateMongoCommand.PROBLEMS_FOUND);
        assertThat(output).contains("  Watches broken, entity: unknown entity type 'owl:Class'")
                          .contains("Problems found, see above.");
    }

    @Test
    void shouldPrintUsage(CapturedOutput output) {
        assertThat(run("migrate-mongo", "--help")).isZero();
        assertThat(plain(output.getOut())).contains("Usage: wp-cli migrate-mongo").contains("--dry-run");

        assertThat(run()).isEqualTo(2);
        assertThat(plain(output.getErr())).contains("Missing command").contains("migrate-mongo");
    }

    /**
     * Runs the command line as an operator would, giving the database as a Spring Boot argument.
     */
    private int run(String... args) {
        var commandLine = new ArrayList<>(List.of(args));
        commandLine.add("--spring.data.mongodb.uri=" + MongoTestServer.uri(databaseName));
        var context = new SpringApplicationBuilder(WebProtegeCli.class).run(commandLine.toArray(String[]::new));
        return SpringApplication.exit(context);
    }

    /**
     * The output without picocli's ANSI styling.
     */
    private static String plain(String output) {
        return output.replaceAll("\u001B\\[[;\\d]*m", "");
    }
}
