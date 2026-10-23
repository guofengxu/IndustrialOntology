package org.industrial.ontology.app.search.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.search.EntitySearchFilter;
import org.industrial.ontology.domain.search.EntitySearchFilterId;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkArgument;
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

    /**
     * The project that has a filter with the id, if any.
     */
    @Nonnull
    public Optional<ProjectId> findProjectOf(@Nonnull EntitySearchFilterId filterId) {
        readLock.lock();
        try {
            var document = collection.find(new Document(EntitySearchFilter.ID, filterId.getId())).first();
            return Optional.ofNullable(document)
                           .map(found -> found.getString(EntitySearchFilter.PROJECT_ID))
                           .map(ProjectId::get);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Makes the filters the project's only filters: the project's filters that are not in the list are deleted, the
     * others inserted or replaced. Every filter must be of the project; the caller checks that no filter id belongs
     * to another project. Not atomic: Mongo has no transactions here (single server), as for the legacy repository.
     */
    public void replaceSearchFilters(@Nonnull ProjectId projectId, @Nonnull List<EntitySearchFilter> filters) {
        checkArgument(filters.stream().allMatch(filter -> filter.getProjectId().equals(projectId)),
                      "Every filter must be of project %s", projectId);
        writeLock.lock();
        try {
            if (!filters.isEmpty()) {
                // Matching the project too: should another project have the id after all, the upsert inserts a
                // second document with the id, which the unique _id refuses, instead of replacing that filter
                collection.bulkWrite(filters.stream()
                                            .map(mapper::toDocument)
                                            .map(document -> new ReplaceOneModel<>(
                                                    new Document(EntitySearchFilter.ID,
                                                                 document.get(EntitySearchFilter.ID))
                                                            .append(EntitySearchFilter.PROJECT_ID,
                                                                    projectId.getId()),
                                                    document,
                                                    new ReplaceOptions().upsert(true)))
                                            .toList());
            }
            var kept = filters.stream().map(filter -> filter.getId().getId()).toList();
            collection.deleteMany(Filters.and(Filters.eq(EntitySearchFilter.PROJECT_ID, projectId.getId()),
                                              Filters.nin(EntitySearchFilter.ID, kept)));
        } finally {
            writeLock.unlock();
        }
    }
}
