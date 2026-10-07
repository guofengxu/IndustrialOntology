package org.industrial.ontology.domain.project;



import java.io.Serializable;
import org.industrial.ontology.domain.core.ProjectId;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.shared.project.ProjectDocumentExistsException}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/06/2012
 */
public class ProjectDocumentExistsException extends ProjectAlreadyExistsException implements Serializable {

    private ProjectDocumentExistsException() {
    }

    public ProjectDocumentExistsException(ProjectId projectId) {
        super(projectId, "Project document already exists: " + projectId.getId());
    }

}
