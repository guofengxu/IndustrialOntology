package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.tag.Tag;

import java.util.Collection;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.tag.ProjectTagsChangedEvent}.
 */
@JsonTypeName("ProjectTagsChanged")
public record ProjectTagsChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("projectTags") Collection<Tag> projectTags) implements ProjectEvent {

    public ProjectTagsChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        projectTags = ImmutableSet.copyOf(projectTags);
    }

    public Collection<Tag> getProjectTags() {
        return projectTags;
    }
}
