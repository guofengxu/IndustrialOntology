package org.industrial.ontology.app.project;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.persistence.MongoPersistenceTestContext;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.kernel.io.download.DownloadFormat;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.industrial.ontology.app.project.ProjectServiceIT.ALICE;
import static org.industrial.ontology.app.project.ProjectServiceIT.BOB;
import static org.industrial.ontology.domain.core.BuiltInRole.CAN_VIEW;
import static org.industrial.ontology.domain.core.BuiltInRole.LAYOUT_EDITOR;

/**
 * {@link ProjectDownloadService}, the legacy {@code ProjectDownloadService} with its {@code CreateDownloadTask}.
 */
class ProjectDownloadServiceIT {

    @TempDir
    static Path dataDirectory;

    private static MongoPersistenceTestContext context;

    @TempDir
    Path sources;

    private ProjectTestFixture fixture;

    private ProjectDownloadService downloadService;

    @BeforeAll
    static void start() {
        context = MongoPersistenceTestContext.startWithProjects(dataDirectory);
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @BeforeEach
    void setUp() {
        context.clear();
        fixture = new ProjectTestFixture(context);
        downloadService = context.bean(ProjectDownloadService.class);
    }

    @Test
    void theHeadRevisionShouldBeDownloadedAndCached() throws Exception {
        var projectId = fixture.createPizzaProject(ALICE, sources);

        var download = downloadService.download(ALICE, projectId, RevisionNumber.getHeadRevisionNumber(),
                                                DownloadFormat.RDF_XML);

        assertThat(download.fileName()).isEqualTo("pizza-ontologies.owl.zip");
        assertThat(download.file().getFileName().toString()).isEqualTo(projectId.getId() + "-R1.owl.zip");
        var entries = entries(download.file());
        assertThat(entries).singleElement().satisfies(entry -> assertThat(entry).endsWith(".owl"));
        assertThat(axiomCount(download.file())).isEqualTo(PizzaOntology.AXIOM_COUNT);
        assertThat(partialFiles(download.file().getParent())).isEmpty();

        // A second request is served from the cache, whatever is in it
        Files.writeString(download.file(), "cached", StandardCharsets.UTF_8);
        var again = downloadService.download(ALICE, projectId, RevisionNumber.getRevisionNumber(1),
                                             DownloadFormat.RDF_XML);
        assertThat(again.file()).isEqualTo(download.file());
        assertThat(Files.readString(again.file())).isEqualTo("cached");
        assertThat(again.fileName()).isEqualTo("pizza-revision-1-ontologies.owl.zip");
    }

    @Test
    void revisionsThatTheProjectDoesNotHaveShouldBeNotFound() {
        var projectId = fixture.createPizzaProject(ALICE, sources);

        for (var revision : List.of(2L, -1L)) {
            assertThatThrownBy(() -> downloadService.download(ALICE, projectId,
                                                              RevisionNumber.getRevisionNumber(revision),
                                                              DownloadFormat.RDF_TURLE))
                    .isInstanceOf(WpException.class)
                    .extracting("code", "status").containsExactly(ProjectDownloadService.REVISION_NOT_FOUND, 404);
        }
    }

    @Test
    void anEmptyProjectThatIsNotLoadedShouldDownloadItsRevisionZero() throws Exception {
        var projectId = fixture.createProject(ALICE, "Empty project");
        context.bean(ProjectRegistry.class).close(projectId);

        var download = downloadService.download(ALICE, projectId, RevisionNumber.getHeadRevisionNumber(),
                                                DownloadFormat.FUNCTIONAL_SYNTAX);

        assertThat(download.file().getFileName().toString()).isEqualTo(projectId.getId() + "-R0.ofn.zip");
        assertThat(download.fileName()).isEqualTo("empty-project-ontologies.ofn.zip");
        assertThat(entries(download.file())).singleElement();
    }

    @Test
    void downloadsShouldNeedDownloadProject() {
        var projectId = fixture.createProject(ALICE, "Pizza");
        fixture.grantProjectRoles(BOB, projectId, LAYOUT_EDITOR);

        assertThatThrownBy(() -> downloadService.download(BOB, projectId, RevisionNumber.getHeadRevisionNumber(),
                                                          DownloadFormat.RDF_XML))
                .isInstanceOf(PermissionDeniedException.class)
                .hasMessageContaining("DownloadProject");
        fixture.grantProjectRoles(BOB, projectId, CAN_VIEW);
        assertThat(downloadService.download(BOB, projectId, RevisionNumber.getHeadRevisionNumber(),
                                            DownloadFormat.RDF_XML).file()).exists();
        assertThatThrownBy(() -> downloadService.download(ALICE, ProjectId.get(UUID.randomUUID().toString()),
                                                          RevisionNumber.getHeadRevisionNumber(),
                                                          DownloadFormat.RDF_XML))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    /**
     * The temporary files that a download is written to before it is moved into the cache.
     */
    private static List<Path> partialFiles(Path directory) throws IOException {
        try (var files = Files.list(directory)) {
            return files.filter(file -> file.getFileName().toString().endsWith(".part")).toList();
        }
    }

    private static List<String> entries(Path file) throws IOException {
        var names = new ArrayList<String>();
        try (var zip = new ZipInputStream(Files.newInputStream(file))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                names.add(entry.getName());
            }
        }
        return names;
    }

    private static int axiomCount(Path file) throws IOException, OWLOntologyCreationException {
        var configuration = new OWLOntologyLoaderConfiguration()
                .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        byte[] document;
        try (var zip = new ZipInputStream(Files.newInputStream(file))) {
            zip.getNextEntry();
            document = zip.readAllBytes();
        }
        var source = new StreamDocumentSource(new ByteArrayInputStream(document), IRI.create("urn:download"),
                                              DownloadFormat.RDF_XML.getDocumentFormat(), null);
        return OWLManager.createOWLOntologyManager()
                         .loadOntologyFromOntologyDocument(source, configuration)
                         .getAxiomCount();
    }
}
