package org.industrial.ontology.kernel.lucene;



import org.industrial.ontology.domain.core.ProjectId;
/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.shortform.ProjectIdModule}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2020-07-14
 */
public class ProjectIdModule {

    private final ProjectId projectId;

    public ProjectIdModule(ProjectId projectId) {
        this.projectId = projectId;
    }


    public ProjectId getProjectId() {
        return projectId;
    }
}
