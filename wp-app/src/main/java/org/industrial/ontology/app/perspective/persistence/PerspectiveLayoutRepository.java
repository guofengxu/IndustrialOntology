package org.industrial.ontology.app.perspective.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.perspective.PerspectiveId;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.PERSPECTIVE_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.PROJECT_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveLayoutDocument.USER_ID;

/**
 * The {@code PerspectiveLayouts} collection; ported from the legacy {@code PerspectiveLayoutRepositoryImpl}. There is
 * at most one layout per {@code (projectId, userId, perspectiveId)}; a {@code null} project and user is the
 * application-wide layout and a {@code null} user the project's default.
 */
public class PerspectiveLayoutRepository {

    public static final String COLLECTION = "PerspectiveLayouts";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    public PerspectiveLayoutRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    @Nonnull
    public Optional<PerspectiveLayoutDocument> findLayout(@Nonnull ProjectId projectId,
                                                          @Nonnull UserId userId,
                                                          @Nonnull PerspectiveId perspectiveId) {
        return findLayout(query(checkNotNull(projectId), checkNotNull(userId), perspectiveId));
    }

    @Nonnull
    public Optional<PerspectiveLayoutDocument> findLayout(@Nonnull ProjectId projectId,
                                                          @Nonnull PerspectiveId perspectiveId) {
        return findLayout(query(checkNotNull(projectId), null, perspectiveId));
    }

    @Nonnull
    public Optional<PerspectiveLayoutDocument> findLayout(@Nonnull PerspectiveId perspectiveId) {
        return findLayout(query(null, null, perspectiveId));
    }

    public void saveLayout(@Nonnull PerspectiveLayoutDocument layout) {
        saveLayouts(List.of(layout));
    }

    /**
     * Inserts the layouts, or replaces those with the same project, user and perspective, in one bulk write.
     */
    public void saveLayouts(@Nonnull List<PerspectiveLayoutDocument> layouts) {
        if (layouts.isEmpty()) {
            return;
        }
        var writes = layouts.stream()
                            .map(layout -> new ReplaceOneModel<>(
                                    query(layout.projectId(), layout.userId(), layout.perspectiveId()),
                                    mapper.toDocument(layout),
                                    new ReplaceOptions().upsert(true)))
                            .toList();
        collection.bulkWrite(writes);
    }

    public void dropLayout(@Nonnull ProjectId projectId, @Nonnull UserId userId, @Nonnull PerspectiveId perspectiveId) {
        collection.deleteOne(query(projectId, userId, perspectiveId));
    }

    public void dropAllLayouts(@Nonnull ProjectId projectId, @Nonnull UserId userId) {
        collection.deleteMany(new Document(PROJECT_ID, projectId.getId()).append(USER_ID, userId.getUserName()));
    }

    private Optional<PerspectiveLayoutDocument> findLayout(Document query) {
        return Optional.ofNullable(collection.find(query).first())
                       .map(document -> mapper.fromDocument(document, PerspectiveLayoutDocument.class));
    }

    private static Document query(@Nullable ProjectId projectId,
                                  @Nullable UserId userId,
                                  @Nonnull PerspectiveId perspectiveId) {
        return new Document(PROJECT_ID, projectId == null ? null : projectId.getId())
                .append(USER_ID, userId == null ? null : userId.getUserName())
                .append(PERSPECTIVE_ID, perspectiveId.getId());
    }
}
