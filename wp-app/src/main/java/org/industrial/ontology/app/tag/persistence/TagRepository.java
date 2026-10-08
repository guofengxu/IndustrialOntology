package org.industrial.ontology.app.tag.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;
import org.industrial.ontology.domain.tag.TagId;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * The {@code Tags} collection; ported from the legacy {@code TagRepositoryImpl}. The documents are {@link Tag}s
 * written through Jackson, keyed by the tag id; a project's labels are unique. The legacy repository was created per
 * project; this one serves all projects and takes the project id where the legacy one used its own.
 */
public class TagRepository {

    public static final String COLLECTION = "Tags";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public TagRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    /**
     * Inserts the tag, or replaces the stored tag with the same id.
     */
    public void saveTag(@Nonnull Tag tag) {
        writeLock.lock();
        try {
            collection.replaceOne(byTagId(tag.getTagId()), mapper.toDocument(tag), new ReplaceOptions().upsert(true));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * {@link #saveTag} for each tag, in one bulk write.
     */
    public void saveTags(@Nonnull Iterable<Tag> tags) {
        writeLock.lock();
        try {
            var writes = Streams.stream(tags)
                                .map(tag -> new ReplaceOneModel<>(byTagId(tag.getTagId()),
                                                                  mapper.toDocument(tag),
                                                                  new ReplaceOptions().upsert(true)))
                                .toList();
            if (!writes.isEmpty()) {
                collection.bulkWrite(writes);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void deleteTag(@Nonnull TagId tagId) {
        writeLock.lock();
        try {
            collection.deleteOne(byTagId(tagId));
        } finally {
            writeLock.unlock();
        }
    }

    @Nonnull
    public ImmutableList<Tag> findTags(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            return Streams.stream(collection.find(new Document(Tag.PROJECT_ID, projectId.getId())))
                          .map(document -> mapper.fromDocument(document, Tag.class))
                          .collect(toImmutableList());
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    public Optional<Tag> findTagByTagId(@Nonnull TagId tagId) {
        readLock.lock();
        try {
            return Optional.ofNullable(collection.find(byTagId(tagId)).limit(1).first())
                           .map(document -> mapper.fromDocument(document, Tag.class));
        } finally {
            readLock.unlock();
        }
    }

    private static Document byTagId(TagId tagId) {
        return new Document(Tag.ID, tagId.getId());
    }
}
