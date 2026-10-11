package org.industrial.ontology.compat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Pins the legacy Mongo samples that wp-app's persistence tests import (docs/01 §5.3, docs/07 5.3-3): the files under
 * {@code wp-app/src/test/resources/legacy-mongo} must be exactly what the legacy persistence code writes for the
 * values in {@link LegacyMongoDocuments}. wp-app then shows that the ported repositories read these documents and
 * write them back unchanged, so the two together tie the ported repositories to the legacy storage format.
 * <p>
 * Run with {@code -Dwp.compat.writeMongoSamples=true} to regenerate the files after changing
 * {@link LegacyMongoDocuments}.
 */
public class LegacyMongoDocumentsIT {

    private static final Path SAMPLES = Path.of(System.getProperty("wp.compat.mongoSamples",
                                                                   "../wp-app/src/test/resources/legacy-mongo"));

    @Test
    public void samplesShouldBeWhatLegacyPersistenceWrites() throws IOException {
        var documents = new LegacyMongoDocuments().all();
        assertThat(documents.size(), is(19));
        if (Boolean.getBoolean("wp.compat.writeMongoSamples")) {
            Files.createDirectories(SAMPLES);
            for (var collection : documents.entrySet()) {
                write(sample(collection.getKey()), collection.getValue());
            }
        }
        for (var collection : documents.entrySet()) {
            List<String> committed = Files.readAllLines(sample(collection.getKey()), StandardCharsets.UTF_8);
            assertThat(collection.getKey(), committed, is(collection.getValue()));
        }
    }

    /**
     * {@code indexes.json} is the index set that wp-app's {@code LegacyCollection} declares and migrate-mongo creates.
     */
    @Test
    public void indexesShouldBeWhatLegacyPersistenceDeclares() throws IOException {
        var indexes = new LegacyMongoDocuments().indexes();
        if (Boolean.getBoolean("wp.compat.writeMongoSamples")) {
            Files.createDirectories(SAMPLES);
            write(SAMPLES.resolve("indexes.json"), indexes);
        }
        assertThat(Files.readAllLines(SAMPLES.resolve("indexes.json"), StandardCharsets.UTF_8), is(indexes));
    }

    private static Path sample(String collection) {
        return SAMPLES.resolve(collection + ".json");
    }

    /**
     * One line per document, with LF line ends on every platform, as the repository stores text files.
     */
    private static void write(Path file, List<String> lines) throws IOException {
        Files.writeString(file, String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
    }
}
