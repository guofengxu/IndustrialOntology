package org.industrial.ontology.app.project.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import java.time.Instant;

/**
 * A document of the {@code ProjectAccess} collection: how often and when a user last opened a project. The legacy
 * {@code ProjectAccessManagerImpl} only ever upserted it, with {@code $inc count} and {@code $set accessed}, so the
 * document is {@code {_id: ObjectId, projectId, userId, accessed: Date, count: Int32}}.
 */
@Document(ProjectAccessDocument.COLLECTION)
public record ProjectAccessDocument(@Id @Nonnull ObjectId id,
                                    @Nonnull String projectId,
                                    @Nonnull String userId,
                                    @Nonnull Instant accessed,
                                    int count) {

    public static final String COLLECTION = "ProjectAccess";

    public static final String PROJECT_ID = "projectId";

    public static final String USER_ID = "userId";

    public static final String ACCESSED = "accessed";

    public static final String COUNT = "count";
}
