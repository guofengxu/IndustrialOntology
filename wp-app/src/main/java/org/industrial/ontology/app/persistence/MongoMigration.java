package org.industrial.ontology.app.persistence;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Prepares a legacy {@code webprotege} database for the ported application (docs/01 §5.3, wp-cli
 * {@code migrate-mongo}). It does three things and nothing else:
 * <ol>
 *     <li>removes the {@code className} field that old Morphia versions stored: at the top level of every document,
 *     and also in the embedded objects of the collections Morphia wrote ({@link LegacyCollection.Storage#MORPHIA}),
 *     but never inside Jackson data, where a field of that name would be the data's own;</li>
 *     <li>creates the legacy indexes that are missing ({@link MongoIndexes});</li>
 *     <li>checks the entities embedded in the Morphia format ({@link OwlEntityMongoCodec}) and reports the documents
 *     whose entity cannot be read, without changing them.</li>
 * </ol>
 * Running it again changes nothing more, so it is safe to repeat. A dry run reports the same without writing. Run it
 * while the server is stopped: a document is rewritten as a whole when an embedded {@code className} is removed.
 */
public final class MongoMigration {

    public static final String CLASS_NAME = "className";

    private final MongoDatabase database;

    private final MongoIndexes indexes;

    public MongoMigration(@Nonnull MongoDatabase database) {
        this.database = checkNotNull(database);
        this.indexes = new MongoIndexes(database);
    }

    /**
     * A document whose embedded entity cannot be read.
     */
    public record Anomaly(@Nonnull LegacyCollection collection,
                          @Nonnull Object documentId,
                          @Nonnull String field,
                          @Nonnull String problem) {
    }

    /**
     * What a run did, or would do in a dry run.
     *
     * @param classNameRemovals by collection, the number of documents that had a {@code className} field
     */
    public record Report(boolean dryRun,
                         @Nonnull Map<LegacyCollection, Long> classNameRemovals,
                         @Nonnull List<MongoIndexes.Result> indexes,
                         @Nonnull List<Anomaly> anomalies) {

        public Report {
            classNameRemovals = Map.copyOf(classNameRemovals);
            indexes = List.copyOf(indexes);
            anomalies = List.copyOf(anomalies);
        }

        /**
         * Whether the database still needs attention: unreadable entities, or indexes that could not be created.
         */
        public boolean hasProblems() {
            return !anomalies.isEmpty()
                    || indexes.stream().anyMatch(result -> result.outcome() == MongoIndexes.Outcome.FAILED);
        }
    }

    @Nonnull
    public Report run(boolean dryRun) {
        var classNameRemovals = new LinkedHashMap<LegacyCollection, Long>();
        var anomalies = new ArrayList<Anomaly>();
        for (var collection : LegacyCollection.values()) {
            long removals = collection.storage() == LegacyCollection.Storage.MORPHIA
                    ? migrateMorphiaCollection(collection, dryRun, anomalies)
                    : removeTopLevelClassNames(collection, dryRun);
            classNameRemovals.put(collection, removals);
        }
        return new Report(dryRun, classNameRemovals, indexes.ensureIndexes(dryRun), anomalies);
    }

    private long removeTopLevelClassNames(LegacyCollection collection, boolean dryRun) {
        var mongoCollection = database.getCollection(collection.collectionName());
        var withClassName = Filters.exists(CLASS_NAME);
        if (dryRun) {
            return mongoCollection.countDocuments(withClassName);
        }
        return mongoCollection.updateMany(withClassName, Updates.unset(CLASS_NAME)).getModifiedCount();
    }

    /**
     * Visits every document: removes {@code className} wherever Morphia may have put it, and checks the entity.
     */
    private long migrateMorphiaCollection(LegacyCollection collection, boolean dryRun, List<Anomaly> anomalies) {
        var mongoCollection = database.getCollection(collection.collectionName());
        long removals = 0;
        for (var document : mongoCollection.find()) {
            var id = document.get("_id");
            if (removeClassNames(document)) {
                removals++;
                if (!dryRun) {
                    mongoCollection.replaceOne(Filters.eq("_id", id), document);
                }
            }
            collection.entityField().ifPresent(field -> OwlEntityMongoCodec.problem(document.get(field))
                    .ifPresent(problem -> anomalies.add(new Anomaly(collection, id, field, problem))));
        }
        return removals;
    }

    /**
     * Removes {@code className} from the document and every document nested in it.
     *
     * @return whether anything was removed
     */
    private static boolean removeClassNames(Object value) {
        var removed = false;
        if (value instanceof Document document) {
            removed = document.containsKey(CLASS_NAME);
            document.remove(CLASS_NAME);
            for (var nested : document.values()) {
                removed |= removeClassNames(nested);
            }
        } else if (value instanceof List<?> list) {
            for (var element : list) {
                removed |= removeClassNames(element);
            }
        }
        return removed;
    }
}
