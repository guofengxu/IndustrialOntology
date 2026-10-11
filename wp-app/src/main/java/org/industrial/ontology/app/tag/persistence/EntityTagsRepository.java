package org.industrial.ontology.app.tag.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.TagId;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.tag.persistence.EntityTagsDocument.ENTITY;
import static org.industrial.ontology.app.tag.persistence.EntityTagsDocument.PROJECT_ID;
import static org.industrial.ontology.app.tag.persistence.EntityTagsDocument.TAGS;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code EntityTags} collection; ported from the legacy {@code EntityTagsRepositoryImpl}, which was created per
 * project. There is one document per {@code (projectId, entity)}.
 */
public class EntityTagsRepository {

    private final MongoOperations mongo;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public EntityTagsRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * Stores the entity's tags, replacing what was stored for it. The legacy repository deleted the old document and
     * inserted a new one; replacing it keeps its {@code _id}.
     */
    public void save(@Nonnull EntityTagsDocument entityTags) {
        writeLock.lock();
        try {
            mongo.findAndReplace(byEntity(entityTags.getProjectId(), entityTags.entity()),
                                 entityTags,
                                 FindAndReplaceOptions.options().upsert());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds the tag to the entity's stored tags. Like the legacy repository, this does nothing for an entity that has
     * no stored tags yet; {@link #save} creates them.
     */
    public void addTag(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity, @Nonnull TagId tagId) {
        writeLock.lock();
        try {
            mongo.updateMulti(byEntity(projectId, entity), new Update().addToSet(TAGS, tagId.getId()),
                              EntityTagsDocument.class);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeTag(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity, @Nonnull TagId tagId) {
        writeLock.lock();
        try {
            mongo.updateMulti(byEntity(projectId, entity), new Update().pull(TAGS, tagId.getId()),
                              EntityTagsDocument.class);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes the tag from every entity of the project.
     */
    public void removeTag(@Nonnull ProjectId projectId, @Nonnull TagId tagId) {
        writeLock.lock();
        try {
            mongo.updateMulti(Query.query(where(PROJECT_ID).is(projectId.getId())),
                              new Update().pull(TAGS, tagId.getId()),
                              EntityTagsDocument.class);
        } finally {
            writeLock.unlock();
        }
    }

    @Nonnull
    public Map<OWLEntity, EntityTagsDocument> findAll(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            var result = new LinkedHashMap<OWLEntity, EntityTagsDocument>();
            mongo.find(Query.query(where(PROJECT_ID).is(projectId.getId())), EntityTagsDocument.class)
                 .forEach(entityTags -> result.put(entityTags.entity(), entityTags));
            return result;
        } finally {
            readLock.unlock();
        }
    }

    @Nonnull
    public Optional<EntityTagsDocument> findByEntity(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity) {
        readLock.lock();
        try {
            return Optional.ofNullable(mongo.findOne(byEntity(projectId, entity), EntityTagsDocument.class));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * The entities, in any project, that have the tag; tag ids are unique across projects.
     */
    @Nonnull
    public List<EntityTagsDocument> findByTagId(@Nonnull TagId tagId) {
        readLock.lock();
        try {
            return mongo.find(Query.query(where(TAGS).is(tagId.getId())), EntityTagsDocument.class);
        } finally {
            readLock.unlock();
        }
    }

    private static Query byEntity(ProjectId projectId, OWLEntity entity) {
        return Query.query(where(PROJECT_ID).is(projectId.getId()).and(ENTITY).is(checkNotNull(entity)));
    }
}
