package org.industrial.ontology.app.watch.persistence;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.watch.persistence.WatchDocument.ENTITY;
import static org.industrial.ontology.app.watch.persistence.WatchDocument.PROJECT_ID;
import static org.industrial.ontology.app.watch.persistence.WatchDocument.TYPE;
import static org.industrial.ontology.app.watch.persistence.WatchDocument.USER_ID;
import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * The {@code Watches} collection; ported from the legacy {@code WatchRecordRepositoryImpl}.
 */
public class WatchRepository {

    private final MongoOperations mongo;

    public WatchRepository(@Nonnull MongoOperations mongo) {
        this.mongo = checkNotNull(mongo);
    }

    /**
     * The user's watches in the project.
     */
    @Nonnull
    public List<WatchDocument> findWatches(@Nonnull ProjectId projectId, @Nonnull UserId userId) {
        var query = Query.query(where(PROJECT_ID).is(projectId.getId()).and(USER_ID).is(userId.getUserName()));
        return mongo.find(query, WatchDocument.class);
    }

    /**
     * Every watch in the project on one of the entities.
     */
    @Nonnull
    public List<WatchDocument> findWatches(@Nonnull ProjectId projectId,
                                           @Nonnull Collection<? extends OWLEntity> entities) {
        var query = Query.query(where(PROJECT_ID).is(projectId.getId()).and(ENTITY).in(entities));
        return mongo.find(query, WatchDocument.class);
    }

    /**
     * The user's watches in the project on one of the entities.
     */
    @Nonnull
    public List<WatchDocument> findWatches(@Nonnull ProjectId projectId,
                                           @Nonnull UserId userId,
                                           @Nonnull Collection<? extends OWLEntity> entities) {
        var query = Query.query(where(PROJECT_ID).is(projectId.getId())
                                        .and(USER_ID).is(userId.getUserName())
                                        .and(ENTITY).in(entities));
        return mongo.find(query, WatchDocument.class);
    }

    /**
     * Stores the watch, replacing the user's existing watch on the same entity. The legacy repository upserted with
     * {@code $set} on every field; replacing the document gives the same document and keeps its {@code _id}.
     */
    public void saveWatch(@Nonnull WatchDocument watch) {
        mongo.findAndReplace(byUserAndEntity(watch), watch, FindAndReplaceOptions.options().upsert());
    }

    /**
     * Deletes the watch if one with the same user, entity and type is stored.
     */
    public void deleteWatch(@Nonnull WatchDocument watch) {
        mongo.findAndRemove(byUserAndEntity(watch).addCriteria(where(TYPE).is(watch.type())), WatchDocument.class);
    }

    private static Query byUserAndEntity(WatchDocument watch) {
        return Query.query(where(PROJECT_ID).is(watch.projectId())
                                   .and(USER_ID).is(watch.userId())
                                   .and(ENTITY).is(watch.entity()));
    }
}
