package org.industrial.ontology.domain.projectsettings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.lang.DisplayNameSettings;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.WithProjectId;
import org.industrial.ontology.domain.lang.DictionaryLanguage;
import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.projectsettings.ProjectSettings}.
 * <p>
 * Slack integration settings are not ported (docs/00 D4); legacy JSON carrying them still parses.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 09/07/2012
 */
public record ProjectSettings(@JsonProperty(ProjectSettings.PROJECT_ID) @Nonnull ProjectId projectId, @Nonnull @JsonProperty(ProjectSettings.DISPLAY_NAME) String projectDisplayName, @Nonnull @JsonProperty(ProjectSettings.DESCRIPTION) String projectDescription, @Nonnull @JsonProperty(ProjectSettings.DEFAULT_LANGUAGE) DictionaryLanguage defaultLanguage, @Nonnull @JsonProperty(ProjectSettings.DEFAULT_DISPLAY_NAME_SETTINGS) DisplayNameSettings defaultDisplayNameSettings, @Nonnull @JsonProperty(ProjectSettings.WEBHOOK_SETTINGS) WebhookSettings webhookSettings) implements Serializable, WithProjectId<ProjectSettings> {

    public ProjectSettings {
        Objects.requireNonNull(projectId, "Null projectId");
        Objects.requireNonNull(projectDisplayName, "Null projectDisplayName");
        Objects.requireNonNull(projectDescription, "Null projectDescription");
        Objects.requireNonNull(defaultLanguage, "Null defaultLanguage");
        Objects.requireNonNull(defaultDisplayNameSettings, "Null defaultDisplayNameSettings");
        Objects.requireNonNull(webhookSettings, "Null webhookSettings");
    }

    private static final String PROJECT_ID = "projectId";

    private static final String DISPLAY_NAME = "displayName";

    private static final String DESCRIPTION = "description";

    private static final String DEFAULT_LANGUAGE = "defaultLanguage";

    private static final String DEFAULT_DISPLAY_NAME_SETTINGS = "defaultDisplayNameSettings";

    private static final String WEBHOOK_SETTINGS = "webhookSettings";

    @Nonnull
    @JsonCreator
    public static ProjectSettings get(@Nonnull @JsonProperty(PROJECT_ID) ProjectId projectId, @Nonnull @JsonProperty(DISPLAY_NAME) String displayName, @Nonnull @JsonProperty(DESCRIPTION) String description, @Nonnull @JsonProperty(DEFAULT_LANGUAGE) DictionaryLanguage defaultLanguage, @Nonnull @JsonProperty(DEFAULT_DISPLAY_NAME_SETTINGS) DisplayNameSettings defaultDisplayNameSettings, @Nonnull @JsonProperty(WEBHOOK_SETTINGS) WebhookSettings webhookSettings) {
        return new ProjectSettings(projectId, displayName, description, defaultLanguage, defaultDisplayNameSettings, webhookSettings);
    }

    @Override
    public ProjectSettings withProjectId(@Nonnull ProjectId projectId) {
        return ProjectSettings.get(projectId, getProjectDisplayName(), getProjectDescription(), getDefaultLanguage(), getDefaultDisplayNameSettings(), getWebhookSettings());
    }

    /**
     * Gets the projectId.
     * @return The projectId.  Not {@code null}.
     */
    @Nonnull
    public ProjectId getProjectId() {
        return projectId;
    }

    /**
     * Gets the project display name.
     * @return The project display name.  Not {@code null}.
     */
    @Nonnull
    public String getProjectDisplayName() {
        return projectDisplayName;
    }

    /**
     * Gets the project description.
     * @return The project description as a string.  May be empty. Not {@code null}.
     */
    @Nonnull
    public String getProjectDescription() {
        return projectDescription;
    }

    @Nonnull
    public DictionaryLanguage getDefaultLanguage() {
        return defaultLanguage;
    }

    @Nonnull
    public DisplayNameSettings getDefaultDisplayNameSettings() {
        return defaultDisplayNameSettings;
    }


    @Nonnull
    public WebhookSettings getWebhookSettings() {
        return webhookSettings;
    }
}
