package org.industrial.ontology.kernel.io.upload;

import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.util.TempFileFactory;
import org.industrial.ontology.kernel.util.ZipInputStreamChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The files extracted from an uploaded zip archive do not outlive the upload's processing.
 */
class UploadedOntologiesProcessorTest {

    private static final DocumentId DOCUMENT_ID = new DocumentId("upload");

    private static final String ONTOLOGY = "<?xml version=\"1.0\"?>\n"
            + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
            + "         xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
            + "  <owl:Ontology rdf:about=\"http://example.org/upload/root\"/>\n"
            + "  <owl:Class rdf:about=\"http://example.org/Root\"/>\n"
            + "</rdf:RDF>\n";

    @TempDir
    Path temp;

    private Path extractions;

    @BeforeEach
    void setUp() throws IOException {
        extractions = Files.createDirectory(temp.resolve("extractions"));
    }

    @Test
    void theExtractedFilesShouldBeDeletedOnceTheOntologiesAreLoaded() throws Exception {
        var processor = processorOf(zipWith("root-ontology.owl"));

        assertThat(processor.getUploadedOntologies(DOCUMENT_ID).size(), is(1));
        assertThat(isEmpty(extractions), is(true));
    }

    @Test
    void theExtractedFilesShouldBeDeletedWhenTheArchiveHasNoRootOntologyDocument() throws Exception {
        var processor = processorOf(zipWith("pizza.owl"));

        assertThrows(RootOntologyDocumentNotFoundException.class, () -> processor.getUploadedOntologies(DOCUMENT_ID));
        assertThat(isEmpty(extractions), is(true));
    }

    @Test
    void anEntryOutsideTheExtractionDirectoryShouldBeRefusedAndNothingKept() throws Exception {
        var processor = processorOf(zipWith("../root-ontology.owl"));

        assertThrows(ZipException.class, () -> processor.getUploadedOntologies(DOCUMENT_ID));
        assertThat(isEmpty(extractions), is(true));
    }

    private UploadedOntologiesProcessor processorOf(Path archive) {
        TempFileFactory tempFileFactory = () -> Files.createTempDirectory(extractions, "upload-").toFile();
        return new UploadedOntologiesProcessor(
                documentId -> archive,
                () -> new UploadedProjectSourcesExtractor(new ZipInputStreamChecker(),
                                                          new ZipArchiveProjectSourcesExtractor(
                                                                  tempFileFactory,
                                                                  new RootOntologyDocumentMatcher()),
                                                          new SingleDocumentProjectSourcesExtractor()));
    }

    private Path zipWith(String entryName) throws IOException {
        var archive = temp.resolve("upload.zip");
        try (var zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zip.putNextEntry(new ZipEntry(entryName));
            zip.write(ONTOLOGY.getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return archive;
    }

    private static boolean isEmpty(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files.findAny().isEmpty();
        }
    }
}
