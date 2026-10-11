package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.ApplicationResource}.
 * <p>
 * The application as a whole: the resource of application roles such as {@code SystemAdmin} and
 * {@code ProjectCreator}.
 */
public record ApplicationResource() implements Resource {

    private static final ApplicationResource INSTANCE = new ApplicationResource();

    @Nonnull
    public static ApplicationResource get() {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public Optional<ProjectId> getProjectId() {
        return Optional.empty();
    }

    @Override
    public boolean isApplication() {
        return true;
    }

    @Override
    public boolean isProject() {
        return false;
    }

    @Override
    public boolean isProject(@Nonnull ProjectId projectId) {
        return false;
    }
}
