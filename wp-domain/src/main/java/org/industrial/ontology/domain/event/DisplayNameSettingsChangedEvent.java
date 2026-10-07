package org.industrial.ontology.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.lang.DisplayNameSettings;

import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.lang.DisplayNameSettingsChangedEvent}.
 */
@JsonTypeName("DisplayNameSettingsChanged")
public record DisplayNameSettingsChangedEvent(@JsonProperty("projectId") ProjectId projectId,
        @JsonProperty("displayLanguage") DisplayNameSettings displayLanguage) implements ProjectEvent {

    public DisplayNameSettingsChangedEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(displayLanguage, "displayLanguage");
    }

    public static DisplayNameSettingsChangedEvent get(ProjectId projectId, DisplayNameSettings displayLanguage) {
        return new DisplayNameSettingsChangedEvent(projectId, displayLanguage);
    }

    public DisplayNameSettings getDisplayLanguage() {
        return displayLanguage;
    }
}
