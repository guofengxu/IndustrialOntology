package org.industrial.ontology.domain.perspective;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.WithProjectId;
import org.industrial.ontology.domain.core.UserId;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Objects;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.perspective.PerspectiveCoordinates}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-31
 */
public record PerspectiveCoordinates(@JsonProperty(PerspectiveCoordinates.PROJECT_ID) @Nullable ProjectId projectIdInternal, @JsonProperty(PerspectiveCoordinates.PROJECT_ID) @Nullable UserId userIdInternal, @JsonProperty(PerspectiveCoordinates.PERSPECTIVE_ID) @Nonnull PerspectiveId perspectiveId) implements WithProjectId<PerspectiveCoordinates> {

    public PerspectiveCoordinates {
        Objects.requireNonNull(perspectiveId, "Null perspectiveId");
    }

    public static final String PROJECT_ID = "projectId";

    public static final String USER_ID = "userId";

    public static final String PERSPECTIVE_ID = "perspectiveId";

    @Nonnull
    public static PerspectiveCoordinates get(@Nonnull ProjectId projectId, @Nonnull UserId userId, @Nonnull PerspectiveId perspectiveId) {
        return getInternal(projectId, userId, perspectiveId);
    }

    @Nonnull
    public static PerspectiveCoordinates get(@Nonnull ProjectId projectId, @Nonnull PerspectiveId perspectiveId) {
        return getInternal(projectId, null, perspectiveId);
    }

    @Nonnull
    public static PerspectiveCoordinates get(@Nonnull PerspectiveId perspectiveId) {
        return getInternal(null, null, perspectiveId);
    }

    @Nonnull
    protected static PerspectiveCoordinates getInternal(@JsonProperty(PROJECT_ID) @Nullable ProjectId projectId, @JsonProperty(USER_ID) @Nullable UserId userId, @JsonProperty(PERSPECTIVE_ID) @Nullable PerspectiveId perspectiveId) {
        return new PerspectiveCoordinates(projectId, userId, perspectiveId);
    }

    @Override
    public PerspectiveCoordinates withProjectId(@Nonnull ProjectId projectId) {
        if (getProjectId().isPresent() && getProjectId().get().equals(projectId)) {
            return this;
        }
        return getInternal(getProjectIdInternal(), getUserIdInternal(), getPerspectiveId());
    }

    @JsonIgnore
    @Nonnull
    public Optional<ProjectId> getProjectId() {
        return Optional.ofNullable(getProjectIdInternal());
    }

    @JsonIgnore
    @Nonnull
    public Optional<UserId> getUserId() {
        return Optional.ofNullable(getUserIdInternal());
    }

    @JsonProperty(PROJECT_ID)
    @Nullable
    public ProjectId getProjectIdInternal() {
        return projectIdInternal;
    }

    @JsonProperty(PROJECT_ID)
    @Nullable
    public UserId getUserIdInternal() {
        return userIdInternal;
    }

    @JsonProperty(PERSPECTIVE_ID)
    @Nonnull
    public PerspectiveId getPerspectiveId() {
        return perspectiveId;
    }
}
