package org.industrial.ontology.app.search.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * The {@code EntitySearchFilters} collection; ported from the legacy {@code EntitySearchFilterRepositoryImpl}. The
 * documents are {@link EntitySearchFilter}s written through Jackson, keyed by the filter id. Saving filters does not
 * update the projects' Lucene documents; the search settings service does that (S7).
 */
public class EntitySearchFilterRepository {

    public static final String COLLECTION = "EntitySearchFilters";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public EntitySearchFilterRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    @Nonnull
    public ImmutableList<EntitySearchFilter> getSearchFilters(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            return Streams.stream(collection.find(new Document(EntitySearchFilter.PROJECT_ID, projectId.getId())))
                          .map(document -> mapper.fromDocument(document, EntitySearchFilter.class))
                          .collect(toImmutableList());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Inserts the filters, or replaces those with the same ids, in one bulk write. Filters that are not in the list
     * are kept, as in the legacy repository.
     */
    public void saveSearchFilters(@Nonnull ImmutableList<EntitySearchFilter> filters) {
        if (filters.isEmpty()) {
            return;
        }
        writeLock.lock();
        try {
            var writes = filters.stream()
                                .map(mapper::toDocument)
                                .map(document -> new ReplaceOneModel<>(
                                        new Document(EntitySearchFilter.ID, document.get(EntitySearchFilter.ID)),
                                        document,
                                        new ReplaceOptions().upsert(true)))
                                .toList();
            collection.bulkWrite(writes);
        } finally {
            writeLock.unlock();
        }
    }
}
