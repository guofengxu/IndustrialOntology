package org.industrial.ontology.app.project.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettings;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettingsRepository;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code EntityCrudKitSettings} collection; ported from the legacy {@code ProjectEntityCrudKitSettingsRepository}.
 * The documents are {@link ProjectEntityCrudKitSettings} written through Jackson, keyed by the project id. Their
 * suffix settings carry the Jackson type id {@code _class} (for example {@code "Uuid"}), which is why this collection
 * must never be mapped by Spring Data.
 */
public class MongoEntityCrudKitSettingsRepository implements ProjectEntityCrudKitSettingsRepository {

    public static final String COLLECTION = "EntityCrudKitSettings";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    public MongoEntityCrudKitSettingsRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public Optional<ProjectEntityCrudKitSettings> findOne(@Nonnull ProjectId projectId) {
        return Optional.ofNullable(collection.find(byProjectId(projectId)).limit(1).first())
                       .map(document -> mapper.fromDocument(document, ProjectEntityCrudKitSettings.class));
    }

    @Override
    public void save(@Nonnull ProjectEntityCrudKitSettings settings) {
        collection.replaceOne(byProjectId(settings.getProjectId()),
                              mapper.toDocument(settings),
                              new ReplaceOptions().upsert(true));
    }

    private static Document byProjectId(ProjectId projectId) {
        return new Document(ProjectEntityCrudKitSettings.PROJECT_ID, projectId.getId());
    }
}
