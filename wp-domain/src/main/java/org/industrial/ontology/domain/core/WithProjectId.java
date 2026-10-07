package org.industrial.ontology.domain.core;



import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.WithProjectId}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-08-24
 */
public interface WithProjectId<T> {

    /**
     * Generate a copy replacing the projectId in this object with the specified id.
     * @param projectId The project id to replace the current project Id
     * @return A copy of this object with the project id replaced with the specified project id
     */
    @JsonIgnore
    T withProjectId(@Nonnull ProjectId projectId);
}
