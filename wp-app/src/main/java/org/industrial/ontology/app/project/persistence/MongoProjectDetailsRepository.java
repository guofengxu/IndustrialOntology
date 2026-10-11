package org.industrial.ontology.app.project.persistence;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.jackson.TimestampSerializer;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import org.industrial.ontology.domain.project.ProjectDetails;
import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static org.industrial.ontology.domain.project.ProjectDetails.IN_TRASH;
import static org.industrial.ontology.domain.project.ProjectDetails.MODIFIED_AT;
import static org.industrial.ontology.domain.project.ProjectDetails.MODIFIED_BY;
import static org.industrial.ontology.domain.project.ProjectDetails.OWNER;
import static org.industrial.ontology.domain.project.ProjectDetails.PROJECT_ID;

/**
 * The {@code ProjectDetails} collection; ported from the legacy {@code ProjectDetailsRepository}, including its two
 * Caffeine caches (details and display-name languages by project, at most 2000 projects each). The documents are
 * {@link ProjectDetails} written through Jackson ({@link JacksonDocumentMapper}), keyed by the project id.
 */
public class MongoProjectDetailsRepository implements ProjectDetailsRepository {

    public static final String COLLECTION = "ProjectDetails";

    private static final long MAX_CACHE_SIZE = 2000;

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    private final LoadingCache<ProjectId, ProjectDetails> cache;

    private final LoadingCache<ProjectId, ImmutableList<DictionaryLanguage>> displayLanguagesCache;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public MongoProjectDetailsRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
        this.cache = Caffeine.newBuilder().maximumSize(MAX_CACHE_SIZE).build(this::findOneFromDbOrNull);
        this.displayLanguagesCache = Caffeine.newBuilder()
                                             .maximumSize(MAX_CACHE_SIZE)
                                             .build(this::findDisplayLanguagesForProject);
    }

    @Override
    public Optional<ProjectDetails> findOne(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            return Optional.ofNullable(cache.get(projectId));
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public ImmutableList<DictionaryLanguage> getDisplayNameLanguages(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            return displayLanguagesCache.get(projectId);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsProject(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            return collection.find(withProjectId(projectId)).projection(new Document()).limit(1).first() != null;
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsProjectWithOwner(@Nonnull ProjectId projectId, @Nonnull UserId owner) {
        readLock.lock();
        try {
            var filter = withProjectId(projectId).append(OWNER, owner.getUserName());
            return collection.find(filter).projection(new Document()).limit(1).first() != null;
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    public List<ProjectDetails> findByOwner(@Nonnull UserId owner) {
        readLock.lock();
        try {
            var result = new ArrayList<ProjectDetails>();
            collection.find(new Document(OWNER, owner.getUserName()))
                      .map(document -> mapper.fromDocument(document, ProjectDetails.class))
                      .into(result);
            return result;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Inserts the details, or replaces the stored document of the same project.
     */
    public void save(@Nonnull ProjectDetails projectDetails) {
        writeLock.lock();
        try {
            var projectId = projectDetails.getProjectId();
            collection.replaceOne(withProjectId(projectId),
                                  mapper.toDocument(projectDetails),
                                  new ReplaceOptions().upsert(true));
            cache.invalidate(projectId);
            displayLanguagesCache.invalidate(projectId);
        } finally {
            writeLock.unlock();
        }
    }

    public void setInTrash(@Nonnull ProjectId projectId, boolean inTrash) {
        writeLock.lock();
        try {
            collection.updateOne(withProjectId(projectId), Updates.set(IN_TRASH, inTrash));
            cache.invalidate(projectId);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Records the last change, with the timestamp in the ISO form that {@link ProjectDetails} is written with.
     */
    public void setModified(@Nonnull ProjectId projectId, long modifiedAt, @Nonnull UserId modifiedBy) {
        writeLock.lock();
        try {
            collection.updateOne(withProjectId(projectId),
                                 Updates.combine(Updates.set(MODIFIED_AT, TimestampSerializer.toIsoDateTime(modifiedAt)),
                                                 Updates.set(MODIFIED_BY, modifiedBy.getUserName())));
            cache.invalidate(projectId);
        } finally {
            writeLock.unlock();
        }
    }

    public void delete(@Nonnull ProjectId projectId) {
        writeLock.lock();
        try {
            collection.deleteOne(withProjectId(projectId));
            cache.invalidate(projectId);
            displayLanguagesCache.invalidate(projectId);
        } finally {
            writeLock.unlock();
        }
    }

    @Nullable
    private ProjectDetails findOneFromDbOrNull(@Nonnull ProjectId projectId) {
        var document = collection.find(withProjectId(projectId)).limit(1).first();
        return document == null ? null : mapper.fromDocument(document, ProjectDetails.class);
    }

    private ImmutableList<DictionaryLanguage> findDisplayLanguagesForProject(@Nonnull ProjectId projectId) {
        return findOne(projectId).map(details -> details.getDefaultDisplayNameSettings()
                                                        .getPrimaryDisplayNameLanguages()
                                                        .stream()
                                                        .collect(toImmutableList()))
                                 .orElse(ImmutableList.of());
    }

    private static Document withProjectId(@Nonnull ProjectId projectId) {
        return new Document(PROJECT_ID, projectId.getId());
    }
}
