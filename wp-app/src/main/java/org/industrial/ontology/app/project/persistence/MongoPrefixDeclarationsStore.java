package org.industrial.ontology.app.project.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.project.PrefixDeclarations;
import org.industrial.ontology.kernel.api.port.PrefixDeclarationsStore;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code PrefixDeclarations} collection; ported from the legacy {@code PrefixDeclarationsStore}. The documents
 * are {@link PrefixDeclarations} written through Jackson, keyed by the project id.
 */
public class MongoPrefixDeclarationsStore implements PrefixDeclarationsStore {

    public static final String COLLECTION = "PrefixDeclarations";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    public MongoPrefixDeclarationsStore(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    /**
     * The project's prefixes; none if the project has never saved any.
     */
    @Override
    public PrefixDeclarations find(@Nonnull ProjectId projectId) {
        var document = collection.find(byProjectId(projectId)).first();
        return document == null ? PrefixDeclarations.get(projectId)
                : mapper.fromDocument(document, PrefixDeclarations.class);
    }

    public void save(@Nonnull PrefixDeclarations prefixDeclarations) {
        collection.replaceOne(byProjectId(prefixDeclarations.getProjectId()),
                              mapper.toDocument(prefixDeclarations),
                              new ReplaceOptions().upsert(true));
    }

    private static Document byProjectId(ProjectId projectId) {
        return new Document(PrefixDeclarations.PROJECT_ID, projectId.getId());
    }
}
