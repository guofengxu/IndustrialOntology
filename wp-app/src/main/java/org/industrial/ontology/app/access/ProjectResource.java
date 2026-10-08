package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.ProjectResource}.
 * <p>
 * One project: the resource of project roles such as {@code CanEdit}.
 */
public record ProjectResource(@Nonnull ProjectId projectId) implements Resource {

    public ProjectResource {
        checkNotNull(projectId);
    }

    @Nonnull
    public static ProjectResource of(@Nonnull ProjectId projectId) {
        return new ProjectResource(projectId);
    }

    @Nonnull
    public static ProjectResource forProject(@Nonnull ProjectId projectId) {
        return of(projectId);
    }

    @Nonnull
    @Override
    public Optional<ProjectId> getProjectId() {
        return Optional.of(projectId);
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public boolean isProject() {
        return true;
    }

    @Override
    public boolean isProject(@Nonnull ProjectId projectId) {
        return this.projectId.equals(projectId);
    }
}
