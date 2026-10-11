package org.industrial.ontology.app.persistence;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The legacy Mongo samples in {@code legacy-mongo/}: one file per collection, one document per line in canonical
 * Extended JSON (as {@code mongoexport --jsonFormat=canonical} writes it), plus {@code indexes.json}. The legacy
 * persistence code wrote them; wp-legacy-compat's {@code LegacyMongoDocumentsIT} checks that they are still exactly
 * what it writes.
 */
public final class LegacyMongoSamples {

    public static final JsonWriterSettings CANONICAL = JsonWriterSettings.builder().outputMode(JsonMode.EXTENDED)
                                                                         .build();

    private LegacyMongoSamples() {
    }

    /**
     * The canonical Extended JSON lines of the collection's sample.
     */
    public static List<String> lines(String collection) {
        var resource = "/legacy-mongo/" + collection + ".json";
        try (var in = LegacyMongoSamples.class.getResourceAsStream(resource)) {
            Objects.requireNonNull(in, resource);
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).lines()
                                                                                       .filter(line -> !line.isBlank())
                                                                                       .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<Document> documents(String collection) {
        return lines(collection).stream().map(Document::parse).toList();
    }

    /**
     * Inserts the collection's sample as it is.
     */
    public static void insert(MongoDatabase database, String collection) {
        database.getCollection(collection).insertMany(documents(collection));
    }

    /**
     * The collection's documents in {@code _id} order, which is also the order of every sample, as canonical
     * Extended JSON.
     */
    public static List<String> stored(MongoDatabase database, String collection) {
        return database.getCollection(collection)
                       .find()
                       .sort(new Document("_id", 1))
                       .map(document -> document.toJson(CANONICAL))
                       .into(new ArrayList<>());
    }

    public static String canonical(Document document) {
        return document.toJson(CANONICAL);
    }
}
