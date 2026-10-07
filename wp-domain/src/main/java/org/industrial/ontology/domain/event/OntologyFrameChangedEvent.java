package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.OntologyFrameChangedEvent}.
 */
@JsonTypeName("OntologyFrameChanged")
public record OntologyFrameChangedEvent(@JsonProperty("ontologyId") OWLOntologyID ontologyId,
        @JsonProperty("projectId") ProjectId projectId) implements ProjectEvent {

    public OntologyFrameChangedEvent {
        Objects.requireNonNull(ontologyId, "ontologyId");
        Objects.requireNonNull(projectId, "projectId");
    }

    public OWLOntologyID getOntologyID() {
        return ontologyId;
    }
}
