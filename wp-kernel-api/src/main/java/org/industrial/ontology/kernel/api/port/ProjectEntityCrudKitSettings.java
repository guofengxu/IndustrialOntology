package org.industrial.ontology.kernel.api.port;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.persistence.ProjectEntityCrudKitSettings}.
 * <p>
 * The new-entity settings of one project; stored in the {@code EntityCrudKitSettings} collection with the
 * project id as {@code _id} (docs/01 §5.3).
 */
public record ProjectEntityCrudKitSettings(
        @JsonProperty(ProjectEntityCrudKitSettings.PROJECT_ID) ProjectId projectId,
        @JsonProperty(ProjectEntityCrudKitSettings.SETTINGS)
        EntityCrudKitSettings<? extends EntityCrudKitSuffixSettings> settings) {

    public static final String PROJECT_ID = "_id";

    public static final String SETTINGS = "settings";

    public ProjectEntityCrudKitSettings {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(settings, "settings");
    }

    @JsonCreator
    public static ProjectEntityCrudKitSettings get(@JsonProperty(PROJECT_ID) @Nonnull ProjectId projectId,
            @JsonProperty(SETTINGS) @Nonnull EntityCrudKitSettings<? extends EntityCrudKitSuffixSettings> settings) {
        return new ProjectEntityCrudKitSettings(projectId, settings);
    }

    @JsonProperty(PROJECT_ID)
    public ProjectId getProjectId() {
        return projectId;
    }

    @JsonProperty(SETTINGS)
    public EntityCrudKitSettings<? extends EntityCrudKitSuffixSettings> getSettings() {
        return settings;
    }
}
