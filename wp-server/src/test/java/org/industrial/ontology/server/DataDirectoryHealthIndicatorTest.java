package org.industrial.ontology.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Status;

class DataDirectoryHealthIndicatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldBeUpForWritableDirectory() throws IOException {
        var health = new DataDirectoryHealthIndicator(tempDir).health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("path", tempDir.toString());
        try (var files = Files.list(tempDir)) {
            assertThat(files).as("probe file is removed").isEmpty();
        }
    }

    @Test
    void shouldBeDownForMissingDirectory() {
        var missing = tempDir.resolve("missing");

        var health = new DataDirectoryHealthIndicator(missing).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("path", missing.toString());
    }

    @Test
    void shouldBeDownWhenPathIsAFile() throws IOException {
        var file = Files.writeString(tempDir.resolve("not-a-directory"), "x");

        var health = new DataDirectoryHealthIndicator(file).health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
