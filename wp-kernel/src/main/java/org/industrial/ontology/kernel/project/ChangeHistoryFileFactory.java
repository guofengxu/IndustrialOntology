package org.industrial.ontology.kernel.project;



import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;

import java.io.File;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.inject.ChangeHistoryFileFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public class ChangeHistoryFileFactory {

    private static final String CHANGE_DATA_DIRECTORY_NAME = "change-data";

    private static final String CHANGE_DATA_FILE_NAME = "change-data.binary";

    @Nonnull
    private final ProjectDirectoryFactory projectDirectoryFactory;

    public ChangeHistoryFileFactory(@Nonnull ProjectDirectoryFactory projectDirectoryFactory) {
        this.projectDirectoryFactory = checkNotNull(projectDirectoryFactory);
    }

    public File getChangeHistoryFile(@Nonnull ProjectId projectId) {
        checkNotNull(projectId);
        var projectDirectory = projectDirectoryFactory.getProjectDirectory(projectId);
        return new File(new File(projectDirectory, CHANGE_DATA_DIRECTORY_NAME), CHANGE_DATA_FILE_NAME);
    }
}
