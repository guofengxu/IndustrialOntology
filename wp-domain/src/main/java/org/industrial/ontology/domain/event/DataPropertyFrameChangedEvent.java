package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLDataProperty;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.DataPropertyFrameChangedEvent}.
 */
@JsonTypeName("DataPropertyFrameChanged")
public record DataPropertyFrameChangedEvent(@JsonProperty("entity") OWLDataProperty entity,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("userId") UserId userId) implements EntityFrameChangedEvent<OWLDataProperty> {

    public DataPropertyFrameChangedEvent {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(projectId, "projectId");
    }
}
