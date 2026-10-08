package org.industrial.ontology.app.watch.persistence;

import org.bson.types.ObjectId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.watches.Watch;
import org.industrial.ontology.domain.watches.WatchType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * A document of the {@code Watches} collection, as Morphia wrote the legacy {@code WatchRecord}:
 * {@code {_id: ObjectId, projectId, userId, entity: {type, iri}, type: ENTITY | BRANCH}}. There is one per
 * {@code (projectId, userId, entity)}; the entity is stored by
 * {@link org.industrial.ontology.app.persistence.OwlEntityMongoCodec}.
 *
 * @param id server-generated; {@code null} for a watch that has not been stored yet
 */
@Document(WatchDocument.COLLECTION)
public record WatchDocument(@Id @Nullable ObjectId id,
                            @Nonnull String projectId,
                            @Nonnull String userId,
                            @Nonnull OWLEntity entity,
                            @Nonnull WatchType type) {

    public static final String COLLECTION = "Watches";

    public static final String PROJECT_ID = "projectId";

    public static final String USER_ID = "userId";

    public static final String ENTITY = "entity";

    public static final String TYPE = "type";

    public WatchDocument {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(type, "type");
    }

    @Nonnull
    public static WatchDocument of(@Nonnull ProjectId projectId, @Nonnull Watch watch) {
        return new WatchDocument(null, projectId.getId(), watch.getUserId().getUserName(), watch.getEntity(),
                                 watch.getType());
    }

    @Nonnull
    public Watch toWatch() {
        return new Watch(UserId.getUserId(userId), entity, type);
    }
}
