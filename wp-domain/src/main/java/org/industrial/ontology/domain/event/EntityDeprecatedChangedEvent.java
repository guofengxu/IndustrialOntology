package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.EntityDeprecatedChangedEvent}.
 */
@JsonTypeName("EntityDeprecatedChanged")
public record EntityDeprecatedChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("entity") OWLEntity entity,
        @JsonProperty("deprecated") boolean deprecated) implements ProjectEvent {

    public EntityDeprecatedChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(entity, "entity");
    }

    public OWLEntity getEntity() {
        return entity;
    }

    public boolean isDeprecated() {
        return deprecated;
    }
}
