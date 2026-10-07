package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Collection;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.EntityTagsChangedEvent}.
 */
@JsonTypeName("EntityTagsChanged")
public record EntityTagsChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("entity") OWLEntity entity,
        @JsonProperty("tags") Collection<Tag> tags) implements ProjectEvent {

    public EntityTagsChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(entity, "entity");
        tags = ImmutableSet.copyOf(tags);
    }

    public OWLEntity getEntity() {
        return entity;
    }

    public Collection<Tag> getTags() {
        return tags;
    }
}
