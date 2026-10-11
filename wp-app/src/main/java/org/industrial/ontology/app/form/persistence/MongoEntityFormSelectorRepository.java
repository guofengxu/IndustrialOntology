package org.industrial.ontology.app.form.persistence;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.Document;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.form.EntityFormSelector;
import org.industrial.ontology.kernel.api.port.EntityFormSelectorRepository;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code FormSelectors} collection; ported from the legacy {@code EntityFormSelectorRepositoryImpl}. The
 * documents are {@link EntityFormSelector}s written through Jackson, one per {@code (projectId, formId)}.
 */
public class MongoEntityFormSelectorRepository implements EntityFormSelectorRepository {

    public static final String COLLECTION = "FormSelectors";

    private static final String PROJECT_ID = "projectId";

    private static final String FORM_ID = "formId";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    public MongoEntityFormSelectorRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public void save(@Nonnull EntityFormSelector selector) {
        var filter = Filters.and(Filters.eq(PROJECT_ID, selector.getProjectId().getId()),
                                 Filters.eq(FORM_ID, selector.getFormId().getId()));
        collection.findOneAndReplace(filter, mapper.toDocument(selector), new FindOneAndReplaceOptions().upsert(true));
    }

    @Override
    public Stream<EntityFormSelector> findFormSelectors(@Nonnull ProjectId projectId) {
        var selectors = new ArrayList<EntityFormSelector>();
        collection.find(new Document(PROJECT_ID, projectId.getId()))
                  .map(document -> mapper.fromDocument(document, EntityFormSelector.class))
                  .into(selectors);
        return selectors.stream();
    }
}
