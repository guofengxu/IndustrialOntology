package org.industrial.ontology.kernel.manager;

import org.industrial.ontology.kernel.owlapi.WebProtegeOWLManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentFormat;
import java.io.File;
import java.io.IOException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.manager.WebProtegeOntologyManagerSaveBinaryDocumentTestCase}.
 * <p>
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 22/04/2014
 */
public class WebProtegeOntologyManagerSaveBinaryDocumentTest {

    @TempDir
    public Path temporaryFolder;

    private File ontologyDocumentFile;

    @BeforeEach
    public void setUp() throws IOException {
        ontologyDocumentFile = Files.createTempFile(temporaryFolder, "junit", ".tmp").toFile();
    }

    /**
     * Ensures that an ontology can be saved in the binary ontology format.  This doesn't test the format serialization,
     * it tests that the {@link WebProtegeOWLManager} is configured
     * with the ability to save an ontology in that format.
     */
    @Test
    public void shouldSaveOntologyToBinaryDocumentFile() throws OWLOntologyCreationException, OWLOntologyStorageException,
            IOException {
        OWLOntologyManager manager = WebProtegeOWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.createOntology();
        BinaryOWLOntologyDocumentFormat format = new BinaryOWLOntologyDocumentFormat();
        // Saving to an IRI target leaves the file open inside the storer (which locks it on Windows), so write
        // through a stream this test owns; what is under test is that the binary format is registered.
        try (OutputStream out = new FileOutputStream(ontologyDocumentFile)) {
            manager.saveOntology(ontology, format, out);
        }
    }
}
