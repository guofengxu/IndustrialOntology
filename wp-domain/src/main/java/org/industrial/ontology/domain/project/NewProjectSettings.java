package org.industrial.ontology.domain.project;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.NewProjectSettings}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 18/01/2012
 */
public record NewProjectSettings(@JsonProperty(NewProjectSettings.PROJECT_OWNER) @Nonnull UserId getProjectOwner, @JsonProperty(NewProjectSettings.DISPLAY_NAME) @Nonnull String getDisplayName, @JsonProperty(NewProjectSettings.LANG_TAG) @Nonnull String getLangTag, @JsonProperty(NewProjectSettings.DESCRIPTION) @Nonnull String getProjectDescription, @Nullable DocumentId sourceDocument) {

    public NewProjectSettings {
        Objects.requireNonNull(getProjectOwner, "Null getProjectOwner");
        Objects.requireNonNull(getDisplayName, "Null getDisplayName");
        Objects.requireNonNull(getLangTag, "Null getLangTag");
        Objects.requireNonNull(getProjectDescription, "Null getProjectDescription");
    }

    private static final String PROJECT_OWNER = "projectOwner";

    private static final String DISPLAY_NAME = "displayName";

    private static final String LANG_TAG = "langTag";

    private static final String DESCRIPTION = "description";

    /**
     * Creates a NewProjectSettings object that describes the basic settings for a new project and also specifies a
     * set of source documents (via a set of {@link DocumentId} objects) from which to create the project.
     *
     * @param projectOwner       The desired owner of the project.  Not null.
     * @param displayName        The desired project name for the new project.  Not null.
     * @param projectDescription The desired project description for the new project.  Not null.
     * @param sourceDocumentId   A {@link DocumentId} object that should be used to identify the source document with
     *                           which to initialise a project.  May be null.
     * @throws NullPointerException if either projectOwner, displayName, projectDescription or sourceDocumentId are
     *                              null.
     */
    public static NewProjectSettings get(@Nonnull UserId projectOwner, @Nonnull String displayName, @Nonnull String langTag, @Nonnull String projectDescription, @Nonnull DocumentId sourceDocumentId) {
        return new NewProjectSettings(projectOwner, displayName, langTag, projectDescription, sourceDocumentId);
    }

    /**
     * Creates a NewProjectSettings object that describes the basic settings for a new project.
     *
     * @param projectOwner       The desired owner of the project.  Not null.
     * @param displayName        The desired project name for the new project.  Not {@code null}.
     * @param langTag            The langTag to be used for new entities.
     * @param projectDescription The desired project description for the new project.  Not {@code null}.
     * @throws NullPointerException if either projectOwner, displayName or projectDescription are null.
     */
    @JsonCreator
    public static NewProjectSettings get(@JsonProperty(PROJECT_OWNER) UserId projectOwner, @JsonProperty(DISPLAY_NAME) String displayName, @JsonProperty(LANG_TAG) String langTag, @JsonProperty(DESCRIPTION) String projectDescription) {
        return new NewProjectSettings(projectOwner, displayName, langTag, projectDescription, null);
    }

    /**
     * Determines whether of not this new project settings object has a source document associated with it.
     *
     * @return <code>true</code> if there is a source documents associated with this {@link NewProjectSettings} object,
     * otherwise <code>false</code>.
     */
    @JsonIgnore
    public boolean hasSourceDocument() {
        return getSourceDocumentId().isPresent();
    }

    /**
     * Gets a set of {@link DocumentId}s that identify source documents that should be used to create a new project.
     *
     * @return A {@link DocumentId} object identifying a source document.
     */
    @Nonnull
    public Optional<DocumentId> getSourceDocumentId() {
        return Optional.ofNullable(sourceDocument());
    }
}
