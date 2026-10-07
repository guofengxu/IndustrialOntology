package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.NamedIndividualFrameChangedEvent}.
 */
@JsonTypeName("NamedIndividualFrameChanged")
public record NamedIndividualFrameChangedEvent(@JsonProperty("entity") OWLNamedIndividual entity,
        @JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("userId") UserId userId) implements EntityFrameChangedEvent<OWLNamedIndividual> {

    public NamedIndividualFrameChangedEvent {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(projectId, "projectId");
    }
}
