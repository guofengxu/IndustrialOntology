package org.industrial.ontology.app.tag.persistence;

import org.bson.types.ObjectId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.TagId;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * A document of the {@code EntityTags} collection, as Morphia wrote the legacy {@code EntityTags}: the tags assigned
 * to one entity of a project, {@code {_id: ObjectId, projectId, entity: {type, iri}, tags: [tagId]}}. The entity is
 * stored by {@link org.industrial.ontology.app.persistence.OwlEntityMongoCodec}.
 *
 * @param id server-generated; {@code null} for tags that have not been stored yet
 */
@Document(EntityTagsDocument.COLLECTION)
public record EntityTagsDocument(@Id @Nullable ObjectId id,
                                 @Nonnull String projectId,
                                 @Nonnull OWLEntity entity,
                                 @Nonnull List<String> tags) {

    public static final String COLLECTION = "EntityTags";

    public static final String PROJECT_ID = "projectId";

    public static final String ENTITY = "entity";

    public static final String TAGS = "tags";

    public EntityTagsDocument {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(entity, "entity");
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    @Nonnull
    public static EntityTagsDocument of(@Nonnull ProjectId projectId,
                                        @Nonnull OWLEntity entity,
                                        @Nonnull List<TagId> tags) {
        return new EntityTagsDocument(null, projectId.getId(), entity, tags.stream().map(TagId::getId).toList());
    }

    @Nonnull
    public ProjectId getProjectId() {
        return ProjectId.get(projectId);
    }

    @Nonnull
    public List<TagId> getTags() {
        return tags.stream().map(TagId::getId).toList();
    }
}
