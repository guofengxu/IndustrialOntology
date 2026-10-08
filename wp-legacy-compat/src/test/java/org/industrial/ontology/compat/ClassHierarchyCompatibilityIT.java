package org.industrial.ontology.compat;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * P0-06 (docs/05): the ported {@code ClassHierarchyProvider} gives the same roots, and for every class the same
 * children, parents, ancestors, leaf status and paths to the root, as the legacy {@code ClassHierarchyProviderImpl}
 * over the same change history. Differences are written to {@code target/compat-reports}.
 */
public class ClassHierarchyCompatibilityIT {

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
    public void shouldBuildPizzaClassHierarchyAsLegacy() throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory, projectId, CompatDatasets.pizza(PizzaOntology.copyTo(sources)));
        assertSameClassHierarchy("class-hierarchy-pizza", projectId);
    }

    @Test
    public void shouldBuildEditedClassHierarchyAsLegacy() throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory,
                                    projectId,
                                    CompatDatasets.edited(PizzaOntology.copyTo(sources), 300, 13L));
        assertSameClassHierarchy("class-hierarchy-edited", projectId);
    }

    @Test
    public void shouldBuildRealProjectClassHierarchyAsLegacy() throws Exception {
        var projectDirectory = System.getProperty(IndexReplayConsistencyIT.PROJECT_DIRECTORY_PROPERTY, "");
        assumeTrue(!projectDirectory.isBlank(),
                   "Set -D" + IndexReplayConsistencyIT.PROJECT_DIRECTORY_PROPERTY + " to compare a real project");
        var projectId = UUID.randomUUID().toString();
        IndexReplayConsistencyIT.copyChangeHistory(Path.of(projectDirectory), dataDirectory, projectId);
        assertSameClassHierarchy("class-hierarchy-real-project", projectId);
    }

    private void assertSameClassHierarchy(String reportName, String projectId) throws Exception {
        var comparison = new KernelComparison();
        var legacy = LegacyKernel.load(dataDirectory, projectId, legacyIndexUpdates);
        try(var kernel = new ProjectKernelFixture(dataDirectory);
            var ported = kernel.open(ProjectId.get(projectId))) {
            comparison.compareClassHierarchies(legacy.classHierarchy,
                                               ported.hierarchies().classHierarchy(),
                                               KernelComparison.classesOf(ported));
        } finally {
            legacy.dispose();
        }
        var report = comparison.writeReport(reportName, "Class hierarchy compatibility, project " + projectId);
        assertThat(comparison.comparisons(), is(greaterThan(100)));
        assertThat("See " + report.toAbsolutePath(), comparison.differences(), is(empty()));
    }
}
