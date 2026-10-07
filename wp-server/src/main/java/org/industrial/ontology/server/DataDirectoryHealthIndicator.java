package org.industrial.ontology.server;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Reports whether the data directory can be written (docs/01 §7): revisions, uploads and Lucene indexes all live
 * there, so a read-only or missing directory makes every edit fail.
 * <p>
 * Writability is probed by creating and deleting a file, because {@link Files#isWritable} does not reflect ACLs and
 * read-only mounts reliably on every platform.
 */
public class DataDirectoryHealthIndicator implements HealthIndicator {

    private final Path dataDirectory;

    public DataDirectoryHealthIndicator(Path dataDirectory) {
        this.dataDirectory = Objects.requireNonNull(dataDirectory);
    }

    @Override
    public Health health() {
        var health = Health.unknown().withDetail("path", dataDirectory.toString());
        if (!Files.isDirectory(dataDirectory)) {
            return health.down().withDetail("error", "Not a directory").build();
        }
        try {
            var probe = Files.createTempFile(dataDirectory, ".health-", ".tmp");
            Files.delete(probe);
            return health.up().build();
        }
        catch (IOException e) {
            return health.down(e).build();
        }
    }
}
