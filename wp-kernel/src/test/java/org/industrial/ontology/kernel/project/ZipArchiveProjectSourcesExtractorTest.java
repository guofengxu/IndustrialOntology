package org.industrial.ontology.kernel.project;

import org.industrial.ontology.kernel.util.TempFileFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.industrial.ontology.kernel.project.FileDocumentSourceMatcher.isFileDocumentSourceForFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import org.industrial.ontology.kernel.io.upload.RawProjectSources;
import org.industrial.ontology.kernel.io.upload.RootOntologyDocumentFileMatcher;
import org.industrial.ontology.kernel.io.upload.ZipArchiveProjectSourcesExtractor;
import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ZipArchiveProjectSourcesExtractor_TestCase}.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ZipArchiveProjectSourcesExtractorTest {

    @TempDir
    public Path temporaryFolder;

    @Mock
    private TempFileFactory tempFileFactory;

    @Mock
    private RootOntologyDocumentFileMatcher rootOntologyDocumentFileMatcher;

    private File outputFolder;

    @BeforeEach
    public void setUp() throws IOException {
        outputFolder = Files.createTempDirectory(temporaryFolder, "junit").toFile();
        when(tempFileFactory.createTempDirectory()).thenReturn(outputFolder);
    }

    @Test
    public void shouldExtractZipFile() throws IOException {
        String document = "/ontologies/root-ontology.owl";
        File expectedDocumentFile = new File(outputFolder, document);
        when(rootOntologyDocumentFileMatcher.isRootOntologyDocument(expectedDocumentFile)).thenReturn(true);
        File zipFile = createZipFile(document);
        ZipArchiveProjectSourcesExtractor extractor = new ZipArchiveProjectSourcesExtractor(tempFileFactory, rootOntologyDocumentFileMatcher);
        RawProjectSources projectSources = extractor.extractProjectSources(zipFile);
        Collection<OWLOntologyDocumentSource> documentSources = projectSources.getDocumentSources();
        assertThat(documentSources, hasSize(1));
        assertThat(documentSources, hasItem(isFileDocumentSourceForFile(expectedDocumentFile)));
    }

    @Test
    public void shouldThrowFileNotFoundExceptionForMissingRootOntology() throws IOException {
        assertThrows(FileNotFoundException.class, () -> {
            String document = "/ontologies/ont.owl";
            File zipFile = createZipFile(document);
            ZipArchiveProjectSourcesExtractor extractor = new ZipArchiveProjectSourcesExtractor(tempFileFactory, rootOntologyDocumentFileMatcher);
            extractor.extractProjectSources(zipFile);
        });
    }

    public File createZipFile(String document) throws IOException {
        File zipFile = Files.createTempFile(temporaryFolder, "junit", ".tmp").toFile();
        OutputStream out = new FileOutputStream(zipFile);
        ZipOutputStream zipOutputStream = new ZipOutputStream(out);
        ZipEntry entryA = new ZipEntry(document);
        entryA.setSize(1);
        zipOutputStream.putNextEntry(entryA);
        zipOutputStream.write(1);
        zipOutputStream.close();
        return zipFile;
    }
}
