package org.industrial.ontology.app.form.persistence;

import com.google.common.collect.ImmutableSet;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.industrial.ontology.app.persistence.JacksonDocumentMapper;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.form.FormDescriptor;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.kernel.api.port.EntityFormRepository;
import org.industrial.ontology.kernel.form.FormDescriptorRecord;
import org.springframework.data.mongodb.core.MongoOperations;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The {@code Forms} collection; ported from the legacy {@code EntityFormRepositoryImpl}. The documents are
 * {@link FormDescriptorRecord}s written through Jackson, one per {@code (projectId, formDescriptor.formId)}; a
 * project's forms are listed in {@code ordinal} order.
 */
public class MongoEntityFormRepository implements EntityFormRepository {

    public static final String COLLECTION = "Forms";

    private static final String PROJECT_ID = FormDescriptorRecord.PROJECT_ID;

    private static final String FORM_DESCRIPTOR_FORM_ID = FormDescriptorRecord.FORM_DESCRIPTOR + ".formId";

    private final MongoCollection<Document> collection;

    private final JacksonDocumentMapper mapper;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock readLock = readWriteLock.readLock();

    private final Lock writeLock = readWriteLock.writeLock();

    public MongoEntityFormRepository(@Nonnull MongoOperations mongo, @Nonnull JacksonDocumentMapper mapper) {
        this.collection = mongo.getCollection(COLLECTION);
        this.mapper = checkNotNull(mapper);
    }

    @Override
    public void deleteFormDescriptor(@Nonnull ProjectId projectId, @Nonnull FormId formId) {
        writeLock.lock();
        try {
            collection.findOneAndDelete(byProjectAndForm(projectId, formId));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Saves the form, keeping its position if it is already stored. A new form goes after the project's other
     * forms; the legacy repository counted the stored copies of this form instead, which is always 0 here, so every
     * new form came first.
     */
    @Override
    public void saveFormDescriptor(@Nonnull ProjectId projectId, @Nonnull FormDescriptor formDescriptor) {
        writeLock.lock();
        try {
            var filter = byProjectAndForm(projectId, formDescriptor.getFormId());
            var existing = collection.find(filter).limit(1).first();
            var ordinal = existing == null ? null : existing.getInteger(FormDescriptorRecord.ORDINAL);
            if (ordinal == null) {
                ordinal = (int) collection.countDocuments(new Document(PROJECT_ID, projectId.getId()));
            }
            var record = FormDescriptorRecord.get(projectId, formDescriptor, ordinal);
            collection.findOneAndReplace(filter, mapper.toDocument(record),
                                         new FindOneAndReplaceOptions().upsert(true));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Replaces all of the project's forms, numbered in list order.
     */
    @Override
    public void setProjectFormDescriptors(@Nonnull ProjectId projectId,
                                          @Nonnull List<FormDescriptor> formDescriptors) {
        writeLock.lock();
        try {
            collection.deleteMany(new Document(PROJECT_ID, projectId.getId()));
            if (formDescriptors.isEmpty()) {
                return;
            }
            var documents = new ArrayList<Document>();
            for (int ordinal = 0; ordinal < formDescriptors.size(); ordinal++) {
                var record = FormDescriptorRecord.get(projectId, formDescriptors.get(ordinal), ordinal);
                documents.add(mapper.toDocument(record));
            }
            collection.insertMany(documents);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Stream<FormDescriptor> findFormDescriptors(@Nonnull ProjectId projectId) {
        readLock.lock();
        try {
            var records = new ArrayList<FormDescriptorRecord>();
            collection.find(new Document(PROJECT_ID, projectId.getId()))
                      .map(document -> mapper.fromDocument(document, FormDescriptorRecord.class))
                      .into(records);
            return records.stream().sorted().map(FormDescriptorRecord::getFormDescriptor);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Stream<FormDescriptor> findFormDescriptors(@Nonnull ImmutableSet<FormId> formIds,
                                                      @Nonnull ProjectId projectId) {
        return findFormDescriptors(projectId).filter(descriptor -> formIds.contains(descriptor.getFormId()));
    }

    @Override
    public Optional<FormDescriptor> findFormDescriptor(@Nonnull ProjectId projectId, @Nonnull FormId formId) {
        readLock.lock();
        try {
            return Optional.ofNullable(collection.find(byProjectAndForm(projectId, formId)).first())
                           .map(document -> mapper.fromDocument(document, FormDescriptorRecord.class))
                           .map(FormDescriptorRecord::getFormDescriptor);
        } finally {
            readLock.unlock();
        }
    }

    private static Bson byProjectAndForm(ProjectId projectId, FormId formId) {
        return Filters.and(Filters.eq(PROJECT_ID, projectId.getId()),
                           Filters.eq(FORM_DESCRIPTOR_FORM_ID, formId.getId()));
    }
}
