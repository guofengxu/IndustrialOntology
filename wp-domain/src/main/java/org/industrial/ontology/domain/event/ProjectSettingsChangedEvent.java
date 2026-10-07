package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.projectsettings.ProjectSettings;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.projectsettings.ProjectSettingsChangedEvent}.
 */
@JsonTypeName("ProjectSettingsChanged")
public record ProjectSettingsChangedEvent(@JsonProperty("projectSettings") ProjectSettings projectSettings) implements ProjectEvent {

    public ProjectSettingsChangedEvent {
        Objects.requireNonNull(projectSettings, "projectSettings");
    }

    @Override
    public ProjectId projectId() {
        return projectSettings.getProjectId();
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }
}
