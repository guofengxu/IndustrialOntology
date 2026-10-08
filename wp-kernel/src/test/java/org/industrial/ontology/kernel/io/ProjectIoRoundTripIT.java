package org.industrial.ontology.kernel.io;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.io.download.DownloadFormat;
import org.industrial.ontology.kernel.io.download.ProjectDownloader;
import org.industrial.ontology.kernel.io.upload.RootOntologyDocumentMatcher;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;

/**
 * Acceptance test of P0-09 (docs/05): a project created by uploading pizza.owl downloads, in every download format,
 * as documents that the OWL API parses back into the uploaded axioms; a zip upload whose root ontology imports a
 * second document keeps both ontologies and the import through the round trip.
 */
public class ProjectIoRoundTripIT {

    private static final IRI MENU_IRI = IRI.create("http://www.industrial-ontology.org/test/menu");

    @TempDir
    Path dataDirectory;

    @TempDir
    Path sources;

    private ProjectKernelFixture kernel;

    private OWLOntology sourcePizza;

    @BeforeEach
    public void setUp() throws Exception {
        kernel = new ProjectKernelFixture(dataDirectory);
        sourcePizza = OWLManager.createOWLOntologyManager()
                                .loadOntologyFromOntologyDocument(PizzaOntology.copyTo(sources).toFile());
    }

    @AfterEach
    public void tearDown() throws Exception {
        kernel.close();
    }

    @ParameterizedTest
    @EnumSource(DownloadFormat.class)
    public void shouldDownloadUploadedOntologyWithTheSameAxioms(DownloadFormat format) throws Exception {
        var projectId = kernel.importProject(sources.resolve("pizza.owl"));

        var documents = download(projectId, format);

        assertThat(documents.keySet(), containsInAnyOrder(PizzaOntology.ONTOLOGY_IRI));
        var pizza = documents.get(PizzaOntology.ONTOLOGY_IRI);
        assertThat(pizza.entryName(), endsWith("." + format.getExtension()));
        if(format == DownloadFormat.MANCHESTER) {
            // The Manchester syntax parser declares every entity that has a frame in the document, so the parsed
            // ontology gains declarations of built-in properties and datatypes; everything else must be identical.
            assertThat(withoutDeclarations(pizza.ontology()), is(withoutDeclarations(sourcePizza)));
            assertThat(pizza.ontology().getAxioms(AxiomType.DECLARATION)
                            .containsAll(sourcePizza.getAxioms(AxiomType.DECLARATION)), is(true));
        }
        else {
            assertThat(pizza.ontology().getAxiomCount(), is(PizzaOntology.AXIOM_COUNT));
            assertThat(pizza.ontology().getAxioms(), is(sourcePizza.getAxioms()));
        }
        assertThat(pizza.ontology().getAnnotations(), is(sourcePizza.getAnnotations()));
        assertThat(pizza.ontology().getOntologyID(), is(sourcePizza.getOntologyID()));
    }

    @Test
    public void shouldKeepImportedOntologyFromZipUpload() throws Exception {
        var menuDocument = sources.resolve(RootOntologyDocumentMatcher.ROOT_ONTOLOGY_DOCUMENT_FILE_NAME);
        try(var out = Files.newOutputStream(menuDocument)) {
            var menuToWrite = createMenuOntology();
            menuToWrite.getOWLOntologyManager().saveOntology(menuToWrite, out);
        }
        // The uploaded axioms are those of the document, which also declares the imported classes it uses.
        var menu = OWLManager.createOWLOntologyManager()
                             .loadOntologyFromOntologyDocument(new FileDocumentSource(menuDocument.toFile()),
                                                               ignoringPizzaImport());
        var zip = sources.resolve("menu.zip");
        try(var out = new ZipOutputStream(Files.newOutputStream(zip))) {
            out.putNextEntry(new ZipEntry("menu/" + RootOntologyDocumentMatcher.ROOT_ONTOLOGY_DOCUMENT_FILE_NAME));
            Files.copy(menuDocument, out);
            out.closeEntry();
            out.putNextEntry(new ZipEntry("menu/imports/pizza.owl"));
            Files.copy(sources.resolve("pizza.owl"), out);
            out.closeEntry();
        }

        var projectId = kernel.importProject(zip);

        try(var context = kernel.open(projectId)) {
            assertThat(context.indexes().get(ProjectOntologiesIndex.class).getOntologyIds().count(), is(2L));
        }
        var documents = download(projectId, DownloadFormat.RDF_TURLE);
        assertThat(documents.keySet(), containsInAnyOrder(MENU_IRI, PizzaOntology.ONTOLOGY_IRI));
        var downloadedMenu = documents.get(MENU_IRI).ontology();
        assertThat(downloadedMenu.getAxioms(), is(menu.getAxioms()));
        assertThat(downloadedMenu.getImportsDeclarations(), is(menu.getImportsDeclarations()));
        assertThat(documents.get(PizzaOntology.ONTOLOGY_IRI).ontology().getAxioms(), is(sourcePizza.getAxioms()));
    }

    /**
     * Downloads the head revision and parses each document of the zip on its own, without fetching imports.
     */
    private Map<IRI, DownloadedDocument> download(ProjectId projectId, DownloadFormat format) throws Exception {
        var bytes = new ByteArrayOutputStream();
        try(var context = kernel.open(projectId)) {
            new ProjectDownloader(projectId,
                                  "Pizza project",
                                  RevisionNumber.getHeadRevisionNumber(),
                                  format,
                                  context.revisionManager(),
                                  kernel.ports().prefixDeclarationsStore())
                    .writeProject(bytes);
        }
        var manager = OWLManager.createOWLOntologyManager();
        var loaderConfiguration = ignoringPizzaImport();
        var documents = new LinkedHashMap<IRI, DownloadedDocument>();
        try(var zip = new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            for(var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                var source = new StreamDocumentSource(CloseShieldInputStream.wrap(zip),
                                                      IRI.create("urn:downloaded:" + entry.getName()),
                                                      format.getDocumentFormat(),
                                                      null);
                var ontology = manager.loadOntologyFromOntologyDocument(source, loaderConfiguration);
                documents.put(ontology.getOntologyID().getOntologyIRI().get(),
                              new DownloadedDocument(entry.getName(), ontology));
            }
        }
        return documents;
    }

    /**
     * Parses each document on its own: the pizza import is not fetched.
     */
    private static OWLOntologyLoaderConfiguration ignoringPizzaImport() {
        return new OWLOntologyLoaderConfiguration()
                .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT)
                .addIgnoredImport(PizzaOntology.ONTOLOGY_IRI);
    }

    private static Set<OWLAxiom> withoutDeclarations(OWLOntology ontology) {
        return ontology.getAxioms()
                       .stream()
                       .filter(axiom -> !axiom.isOfType(AxiomType.DECLARATION))
                       .collect(Collectors.toSet());
    }

    private static OWLOntology createMenuOntology() throws Exception {
        var manager = OWLManager.createOWLOntologyManager();
        var dataFactory = manager.getOWLDataFactory();
        var menu = manager.createOntology(MENU_IRI);
        manager.applyChange(new AddImport(menu, dataFactory.getOWLImportsDeclaration(PizzaOntology.ONTOLOGY_IRI)));
        var menuItem = dataFactory.getOWLClass(IRI.create(MENU_IRI + "#MenuItem"));
        manager.addAxiom(menu, dataFactory.getOWLDeclarationAxiom(menuItem));
        manager.addAxiom(menu, dataFactory.getOWLAnnotationAssertionAxiom(dataFactory.getRDFSLabel(),
                                                                           menuItem.getIRI(),
                                                                           dataFactory.getOWLLiteral("Menu Item",
                                                                                                     "en")));
        manager.addAxiom(menu, dataFactory.getOWLSubClassOfAxiom(PizzaOntology.cls(dataFactory, "Margherita"),
                                                                 menuItem));
        manager.addAxiom(menu, dataFactory.getOWLSubClassOfAxiom(PizzaOntology.cls(dataFactory, "AmericanHot"),
                                                                 menuItem));
        return menu;
    }

    private record DownloadedDocument(String entryName, OWLOntology ontology) {
    }
}
