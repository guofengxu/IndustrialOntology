package org.industrial.ontology.kernel.project;



import javax.annotation.Nonnull;
import java.io.File;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.inject.OverridableFileFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19 Mar 2017
 */
public class OverridableFileFactory {

    private File dataDirectory;

    public OverridableFileFactory(@Nonnull File dataDirectory) {
        this.dataDirectory = checkNotNull(dataDirectory);
    }

    public OverridableFile getOverridableFile(@Nonnull String relativePathName) {
        return new OverridableFile(relativePathName, dataDirectory);
    }
}
