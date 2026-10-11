package org.industrial.ontology.api.project;

import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;

/**
 * Project ids in paths and parameters are UUIDs (docs/02 §1); anything else is a bad request.
 */
final class ProjectIds {

    private ProjectIds() {
    }

    @Nonnull
    static ProjectId parse(@Nonnull String projectId) {
        if (!ProjectId.isWelFormedProjectId(projectId)) {
            throw WpException.invalidRequest("A project id is a UUID");
        }
        return ProjectId.get(projectId);
    }
}
