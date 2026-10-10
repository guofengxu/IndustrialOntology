package org.industrial.ontology.kernel.io.upload;

import com.google.common.collect.Lists;
import org.industrial.ontology.kernel.owlapi.WebProtegeOWLManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The imports of an uploaded document are resolved only to the other documents of the upload.
 */
class RawProjectSourcesImporterTest {

    private static final IRI SIBLING = IRI.create("http://example.org/upload/sibling");

    private static final IRI OUTSIDE = IRI.create("http://example.org/outside");

    @TempDir
    Path temp;

    /**
     * For documents that no parser can read: OWL API's OBO parser leaves such a document open, so on Windows it cannot
     * be deleted until the stream is collected, and JUnit's clean-up would fail the test.
     */
    @TempDir(cleanup = CleanupMode.NEVER)
    Path unreadableDocuments;

    private OWLOntologyManager manager;

    private Path uploadDirectory;

    private Path outsideDocument;

    @BeforeEach
    void setUp() throws IOException {
        manager = WebProtegeOWLManager.createOWLOntologyManager();
        uploadDirectory = Files.createDirectory(temp.resolve("upload"));
        outsideDocument = temp.resolve("outside.owl");
        Files.writeString(outsideDocument, rdfXml(OUTSIDE, null, "Outside"));
        Files.writeString(uploadDirectory.resolve("sibling.owl"), rdfXml(SIBLING, null, "Sibling"));
    }

    @Test
    void anImportShouldBeLoadedFromTheUpload() throws Exception {
        importUploadWithRootImporting(SIBLING);

        assertThat(manager.contains(SIBLING), is(true));
        assertThat(isClassLoaded("Sibling"), is(true));
    }

    @Test
    void anImportOfALocalFileShouldNotBeLoaded() throws Exception {
        importUploadWithRootImporting(IRI.create(outsideDocument.toUri()));

        assertThat(isClassLoaded("Outside"), is(false));
        assertThat(manager.getOntologies().size(), is(1));
    }

    @Test
    void theManagersOwnMappersShouldNotResolveImportsAndShouldBeRestoredAfterwards() throws Exception {
        OWLOntologyIRIMapper outsideMapper = iri -> iri.equals(OUTSIDE) ? IRI.create(outsideDocument.toUri()) : null;
        manager.getIRIMappers().add(outsideMapper);
        List<OWLOntologyIRIMapper> mappersBefore = Lists.newArrayList(manager.getIRIMappers());
        List<OWLOntologyFactory> factoriesBefore = Lists.newArrayList(manager.getOntologyFactories());
        List<OWLParserFactory> parsersBefore = Lists.newArrayList(manager.getOntologyParsers());

        importUploadWithRootImporting(OUTSIDE);

        assertThat(isClassLoaded("Outside"), is(false));
        assertThat(Lists.newArrayList(manager.getIRIMappers()), is(mappersBefore));
        assertThat(Lists.newArrayList(manager.getOntologyFactories()), is(factoriesBefore));
        assertThat(Lists.newArrayList(manager.getOntologyParsers()), is(parsersBefore));
    }

    /**
     * A JSON-LD document names its context by IRI, and the JSON-LD parser fetches it: an upload could read local files
     * or request other hosts that way, as through {@code owl:imports}. The first key makes the RDF/JSON parser, which
     * OWL API tries before the JSON-LD one, give up with a parse error rather than an unchecked exception.
     */
    @Test
    void aJsonLdContextShouldNotBeFetched() throws Exception {
        var context = temp.resolve("context.jsonld");
        Files.writeString(context, "{\"@context\": {\"@vocab\": \"http://example.org/fromTheContext/\"}}");
        var root = unreadableDocuments.resolve("root.jsonld");
        Files.writeString(root, "{\"http://example.org/label\": \"not an RDF/JSON predicate-object map\",\n"
                + " \"@context\": \"" + context.toUri() + "\",\n"
                + " \"@id\": \"http://example.org/A\",\n"
                + " \"@type\": \"http://www.w3.org/2002/07/owl#Class\",\n"
                + " \"comment\": \"read with the context\"}\n");

        try {
            importUpload(root);
        } catch (OWLOntologyCreationException e) {
            // Refusing the document altogether is fine
        }

        assertThat(manager.getOntologies().stream()
                          .flatMap(ontology -> ontology.getAxioms().stream())
                          .anyMatch(axiom -> axiom.toString().contains("fromTheContext")), is(false));
    }

    /**
     * Rio's RDF/JSON parser throws an unchecked exception for JSON that is not RDF/JSON, which OWL API does not catch:
     * the upload failed as a server error instead of as a document that cannot be read.
     */
    @Test
    void aJsonDocumentThatIsNotAnOntologyShouldBeReportedAsUnparsable() throws Exception {
        var root = unreadableDocuments.resolve("root.json");
        Files.writeString(root, "{\"@context\": {\"name\": \"http://example.org/name\"}, \"name\": \"Pizza\"}\n");

        assertThrows(OWLOntologyCreationException.class, () -> importUpload(root));
    }

    private void importUploadWithRootImporting(IRI importIri) throws IOException, OWLOntologyCreationException {
        var root = uploadDirectory.resolve("root.owl");
        Files.writeString(root, rdfXml(IRI.create("http://example.org/upload/root"), importIri, "Root"));
        importUpload(root);
    }

    private void importUpload(Path root) throws OWLOntologyCreationException {
        var sources = new RawProjectSources() {
            @Override
            public Collection<OWLOntologyDocumentSource> getDocumentSources() {
                return List.of(new FileDocumentSource(root.toFile()));
            }

            @Override
            public OWLOntologyIRIMapper getOntologyIRIMapper() {
                return new AutoIRIMapper(uploadDirectory.toFile(), true);
            }

            @Override
            public void cleanUpTemporaryFiles() {
            }
        };
        var loaderConfig = new OWLOntologyLoaderConfiguration()
                .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        new RawProjectSourcesImporter(manager, loaderConfig).importRawProjectSources(sources);
    }

    private boolean isClassLoaded(String name) {
        var classIri = IRI.create("http://example.org/" + name);
        return manager.getOntologies().stream()
                      .flatMap(ontology -> ontology.getClassesInSignature().stream())
                      .anyMatch(owlClass -> owlClass.getIRI().equals(classIri));
    }

    private static String rdfXml(IRI ontologyIri, IRI importIri, String className) {
        var imports = importIri == null ? "" : "    <owl:imports rdf:resource=\"" + importIri + "\"/>\n";
        return "<?xml version=\"1.0\"?>\n"
                + "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
                + "         xmlns:owl=\"http://www.w3.org/2002/07/owl#\">\n"
                + "  <owl:Ontology rdf:about=\"" + ontologyIri + "\">\n"
                + imports
                + "  </owl:Ontology>\n"
                + "  <owl:Class rdf:about=\"http://example.org/" + className + "\"/>\n"
                + "</rdf:RDF>\n";
    }
}
