package org.industrial.ontology.app.perspective.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument.PROJECT_ID;
import static org.industrial.ontology.app.perspective.persistence.PerspectiveDescriptorsDocument.USER_ID;

/**
 * The {@code PerspectiveDescriptors} collection; ported from the legacy {@code PerspectiveDescriptorRepositoryImpl}.
 * There is at most one list per {@code (projectId, userId)}, either of which may be {@code null}.
 */
public class PerspectiveDescriptorRepository {

    public static final String COLLECTION = "PerspectiveDescriptors";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    public PerspectiveDescriptorRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    /**
     * Inserts the list, or replaces the one with the same project and user.
     */
    public void saveDescriptors(@Nonnull PerspectiveDescriptorsDocument descriptors) {
        collection.replaceOne(query(descriptors.projectId(), descriptors.userId()),
                              mapper.toDocument(descriptors),
                              new ReplaceOptions().upsert(true));
    }

    /**
     * The user's own list for the project.
     */
    @Nonnull
    public Optional<PerspectiveDescriptorsDocument> findDescriptors(@Nonnull ProjectId projectId,
                                                                    @Nonnull UserId userId) {
        return findFirst(query(checkNotNull(projectId), checkNotNull(userId)));
    }

    /**
     * The project's default list.
     */
    @Nonnull
    public Optional<PerspectiveDescriptorsDocument> findDescriptors(@Nonnull ProjectId projectId) {
        return findFirst(query(checkNotNull(projectId), null));
    }

    /**
     * The application-wide list.
     */
    @Nonnull
    public Optional<PerspectiveDescriptorsDocument> findDescriptors() {
        return findFirst(query(null, null));
    }

    /**
     * The project's default list and the application-wide list, whichever exist.
     */
    @Nonnull
    public List<PerspectiveDescriptorsDocument> findProjectAndSystemDescriptors(@Nonnull ProjectId projectId) {
        var projectOrSystem = List.of(new Document(PROJECT_ID, null), new Document(PROJECT_ID, projectId.getId()));
        var query = new Document("$or", projectOrSystem).append(USER_ID, null);
        var result = new ArrayList<PerspectiveDescriptorsDocument>();
        collection.find(query)
                  .map(document -> mapper.fromDocument(document, PerspectiveDescriptorsDocument.class))
                  .into(result);
        return result;
    }

    public void dropAllDescriptors(@Nonnull ProjectId projectId, @Nonnull UserId userId) {
        collection.deleteMany(query(projectId, userId));
    }

    private Optional<PerspectiveDescriptorsDocument> findFirst(Document query) {
        return Optional.ofNullable(collection.find(query).first())
                       .map(document -> mapper.fromDocument(document, PerspectiveDescriptorsDocument.class));
    }

    private static Document query(@Nullable ProjectId projectId, @Nullable UserId userId) {
        return new Document(PROJECT_ID, projectId == null ? null : projectId.getId())
                .append(USER_ID, userId == null ? null : userId.getUserName());
    }
}
