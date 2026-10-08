package org.industrial.ontology.app.persistence;

import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates the legacy indexes ({@link LegacyCollection#indexes()}) that a database lacks. The legacy repositories did
 * this when they were constructed; the server does it once at startup, and migrate-mongo reports what it did.
 * <p>
 * An index counts as present when the collection has an index on the same keys in the same order. Indexes are created
 * with Mongo's default name, as the legacy code created them, so an existing legacy index is never duplicated. If the
 * keys exist with a different uniqueness, or the server refuses the index (for example because existing documents
 * violate a unique index), the index is reported as failed and left alone.
 */
public final class MongoIndexes {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexes.class);

    public enum Outcome {

        /** The collection already has the index. */
        PRESENT,

        /** The index was missing and has been created. */
        CREATED,

        /** The index is missing; reported by a dry run, which creates nothing. */
        MISSING,

        /** The index is missing and could not be created; see {@link Result#problem()}. */
        FAILED
    }

    public record Result(@Nonnull LegacyCollection collection,
                         @Nonnull LegacyCollection.Index index,
                         @Nonnull Outcome outcome,
                         @Nullable String problem) {
    }

    private final MongoDatabase database;

    public MongoIndexes(@Nonnull MongoDatabase database) {
        this.database = checkNotNull(database);
    }

    /**
     * Creates the missing indexes of every legacy collection.
     *
     * @param dryRun only report what is missing
     */
    @Nonnull
    public List<Result> ensureIndexes(boolean dryRun) {
        var results = new ArrayList<Result>();
        for (var collection : LegacyCollection.values()) {
            if (collection.indexes().isEmpty()) {
                continue;
            }
            var existing = new ArrayList<Document>();
            database.getCollection(collection.collectionName()).listIndexes().into(existing);
            for (var index : collection.indexes()) {
                results.add(ensureIndex(collection, index, existing, dryRun));
            }
        }
        return results;
    }

    private Result ensureIndex(LegacyCollection collection,
                               LegacyCollection.Index index,
                               List<Document> existing,
                               boolean dryRun) {
        var sameKeys = existing.stream().filter(candidate -> hasKeys(candidate, index)).findFirst();
        if (sameKeys.isPresent()) {
            var unique = Boolean.TRUE.equals(sameKeys.get().getBoolean("unique"));
            if (unique == index.unique()) {
                return new Result(collection, index, Outcome.PRESENT, null);
            }
            return failed(collection, index, "an index '" + sameKeys.get().getString("name")
                    + "' on the same keys exists with unique=" + unique);
        }
        if (dryRun) {
            return new Result(collection, index, Outcome.MISSING, null);
        }
        try {
            database.getCollection(collection.collectionName())
                    .createIndex(index.keyDocument(), new IndexOptions().unique(index.unique()));
            return new Result(collection, index, Outcome.CREATED, null);
        } catch (MongoException e) {
            return failed(collection, index, e.getMessage());
        }
    }

    private static Result failed(LegacyCollection collection, LegacyCollection.Index index, String problem) {
        logger.warn("Cannot create index {} on {}: {}", index.keys(), collection.collectionName(), problem);
        return new Result(collection, index, Outcome.FAILED, problem);
    }

    private static boolean hasKeys(Document indexDocument, LegacyCollection.Index index) {
        var key = Optional.ofNullable(indexDocument.get("key", Document.class)).orElseGet(Document::new);
        return List.copyOf(key.keySet()).equals(index.keys())
                && key.values().stream().allMatch(direction -> direction instanceof Number number
                        && number.doubleValue() == 1);
    }
}
