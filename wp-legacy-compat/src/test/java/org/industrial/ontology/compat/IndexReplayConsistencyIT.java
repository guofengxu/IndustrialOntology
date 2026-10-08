package org.industrial.ontology.compat;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * P0-05 (docs/05): the same {@code change-data.binary} replayed by the legacy kernel ({@code RootIndexImpl} path:
 * legacy index implementations fed by the legacy {@code IndexUpdater}) and by the ported {@code ProjectContext}
 * gives the same {@code OntologyAxiomsIndex}, {@code ClassFrameAxiomsIndex}, {@code SubClassOfAxiomsBySubClassIndex}
 * and {@code AnnotationAssertionAxiomsBySubjectIndex} results for every signature entity. Differences are written to
 * {@code target/compat-reports}.
 * <p>
 * Datasets: pizza.owl, and the pizza import followed by a history of edits standing in for a real project. A real
 * legacy project directory, the one containing {@code change-data/change-data.binary}, can be added with
 * {@code -Dwp.compat.projectDirectory=<dir>}.
 */
public class IndexReplayConsistencyIT {

    static final String PROJECT_DIRECTORY_PROPERTY = "wp.compat.projectDirectory";

    @TempDir
    Path dataDirectory;

    @TempDir
    Path sources;

    private ExecutorService legacyIndexUpdates;

    @BeforeEach
    public void setUp() {
        legacyIndexUpdates = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    public void tearDown() {
        legacyIndexUpdates.shutdownNow();
    }

    @Test
    public void shouldReplayPizzaIdentically() throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory, projectId, CompatDatasets.pizza(PizzaOntology.copyTo(sources)));
        assertReplaysIdentically("index-replay-pizza", projectId);
    }

    @Test
    public void shouldReplayEditHistoryIdentically() throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory,
                                    projectId,
                                    CompatDatasets.edited(PizzaOntology.copyTo(sources), 300, 11L));
        assertReplaysIdentically("index-replay-edited", projectId);
    }

    @Test
    public void shouldReplayRealProjectIdentically() throws Exception {
        var projectDirectory = System.getProperty(PROJECT_DIRECTORY_PROPERTY, "");
        assumeTrue(!projectDirectory.isBlank(), "Set -D" + PROJECT_DIRECTORY_PROPERTY + " to replay a real project");
        var projectId = UUID.randomUUID().toString();
        copyChangeHistory(Path.of(projectDirectory), dataDirectory, projectId);
        assertReplaysIdentically("index-replay-real-project", projectId);
    }

    /**
     * Guards against a comparison that cannot fail: histories that differ by one revision must be reported.
     */
    @Test
    public void shouldReportDifferencesBetweenDifferentHistories() throws Exception {
        var pizza = PizzaOntology.copyTo(sources);
        var legacyProjectId = UUID.randomUUID().toString();
        var portedProjectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory, legacyProjectId, CompatDatasets.pizza(pizza));
        LegacyKernel.writeRevisions(dataDirectory, portedProjectId, CompatDatasets.edited(pizza, 1, 3L));

        var comparison = new KernelComparison();
        var legacy = LegacyKernel.load(dataDirectory, legacyProjectId, legacyIndexUpdates);
        try(var kernel = new ProjectKernelFixture(dataDirectory);
            var ported = kernel.open(ProjectId.get(portedProjectId))) {
            comparison.compareIndexes(legacy, ported);
        } finally {
            legacy.dispose();
        }

        assertThat(comparison.differences(), is(not(empty())));
        assertThat(String.join("\n", comparison.differences()), containsString("EditClass1"));
    }

    private void assertReplaysIdentically(String reportName, String projectId) throws Exception {
        var comparison = new KernelComparison();
        var legacy = LegacyKernel.load(dataDirectory, projectId, legacyIndexUpdates);
        try(var kernel = new ProjectKernelFixture(dataDirectory);
            var ported = kernel.open(ProjectId.get(projectId))) {
            comparison.compareIndexes(legacy, ported);
        } finally {
            legacy.dispose();
        }
        var report = comparison.writeReport(reportName, "Index replay consistency, project " + projectId);
        assertThat(comparison.comparisons(), is(greaterThan(100)));
        assertThat("See " + report.toAbsolutePath(), comparison.differences(), is(empty()));
    }

    /**
     * Copies a legacy project's change history into {@code dataDirectory} under {@code projectId}.
     */
    static void copyChangeHistory(Path projectDirectory, Path dataDirectory, String projectId) throws Exception {
        var source = projectDirectory.resolve("change-data").resolve("change-data.binary");
        var target = dataDirectory.resolve("data-store")
                                  .resolve("project-data")
                                  .resolve(projectId)
                                  .resolve("change-data")
                                  .resolve("change-data.binary");
        Files.createDirectories(target.getParent());
        Files.copy(source, target);
    }
}
