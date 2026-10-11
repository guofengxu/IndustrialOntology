package org.industrial.ontology.app.project;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A created project download: the zip archive in the download cache and the name that the client should save it
 * under.
 */
public record ProjectDownload(@Nonnull Path file, @Nonnull String fileName) {

    public ProjectDownload {
        checkNotNull(file);
        checkNotNull(fileName);
    }
}
