package org.industrial.ontology.domain.project;



import java.io.Serializable;
import org.industrial.ontology.domain.core.ProjectId;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.ProjectCreationException}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/05/2012
 */
public class ProjectCreationException extends RuntimeException implements Serializable {

    private ProjectId projectId;

    protected ProjectCreationException() {
    }

    public ProjectCreationException(ProjectId projectId, String message) {
        super(message);
        this.projectId = projectId;
    }

    public ProjectId getProjectId() {
        return projectId;
    }
}
