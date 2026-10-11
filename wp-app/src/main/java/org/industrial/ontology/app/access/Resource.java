package org.industrial.ontology.app.access;

import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.access.Resource}.
 * <p>
 * What a role assignment applies to: the application as a whole or one project. In the {@code RoleAssignments}
 * collection a project is stored by id and the application by leaving the project id out.
 */
public sealed interface Resource permits ApplicationResource, ProjectResource {

    /**
     * The project, or empty for the application.
     */
    @Nonnull
    Optional<ProjectId> getProjectId();

    boolean isApplication();

    boolean isProject();

    boolean isProject(@Nonnull ProjectId projectId);
}
