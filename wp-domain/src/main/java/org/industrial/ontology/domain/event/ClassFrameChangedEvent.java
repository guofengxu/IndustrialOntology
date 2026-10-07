package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLClass;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.ClassFrameChangedEvent}.
 */
@JsonTypeName("ClassFrameChanged")
public record ClassFrameChangedEvent(@JsonProperty("entity") OWLClass entity,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("userId") UserId userId) implements EntityFrameChangedEvent<OWLClass> {

    public ClassFrameChangedEvent {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(projectId, "projectId");
    }
}
