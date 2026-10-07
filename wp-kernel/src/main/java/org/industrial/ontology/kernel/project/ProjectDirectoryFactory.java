package org.industrial.ontology.kernel.project;



import org.industrial.ontology.domain.core.ProjectId;
import java.io.File;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.inject.project.ProjectDirectoryFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 23/02/16
 */
public class ProjectDirectoryFactory {

    private final File dataDirectory;

    public ProjectDirectoryFactory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public File getProjectDirectory(ProjectId projectId) {
        return new File(getProjectDataDirectory(), projectId.getId());
    }

    private File getProjectDataDirectory() {
        return new File(getDataStoreDirectory(), "project-data");
    }

    private File getDataStoreDirectory() {
        return new File(dataDirectory, "data-store");
    }

}
