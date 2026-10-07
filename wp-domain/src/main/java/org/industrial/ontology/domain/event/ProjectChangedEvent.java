package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.entity.OWLEntityData;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;

import java.util.Objects;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.event.ProjectChangedEvent}.
 */
@JsonTypeName("ProjectChanged")
public record ProjectChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("revisionSummary") RevisionSummary revisionSummary,
        @JsonProperty("subjects") Set<OWLEntityData> subjects) implements ProjectEvent {

    public ProjectChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(revisionSummary, "revisionSummary");
        Objects.requireNonNull(subjects, "subjects");
    }

    public RevisionSummary getRevisionSummary() {
        return revisionSummary;
    }

    @JsonIgnore
    public RevisionNumber getRevisionNumber() {
        return revisionSummary.getRevisionNumber();
    }

    @JsonIgnore
    public UserId getUserId() {
        return revisionSummary.getUserId();
    }

    @JsonIgnore
    public long getTimestamp() {
        return revisionSummary.getTimestamp();
    }

    public Set<OWLEntityData> getSubjects() {
        return subjects;
    }
}
