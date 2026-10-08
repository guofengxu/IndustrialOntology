package org.industrial.ontology.compat;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.api.index.AxiomsByEntityReferenceIndex;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.api.index.EquivalentClassesAxiomsIndex;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.industrial.ontology.kernel.api.index.ProjectSignatureByTypeIndex;
import org.industrial.ontology.kernel.api.index.SubClassOfAxiomsBySubClassIndex;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslatorImpl;
import org.industrial.ontology.kernel.hierarchy.ClassHierarchyProvider;
import org.industrial.ontology.kernel.index.IndexUpdater;
import org.industrial.ontology.kernel.index.ProjectIndexes;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.industrial.ontology.kernel.revision.RevisionManager;
import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.ToLongFunction;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * P0 exit criterion (docs/05): loading a 50k-axiom project takes at most 1.2 times as long as in the legacy system.
 * <p>
 * Both kernels load the same {@code change-data.binary}, written by the legacy store, over the same scope: read the
 * revisions, replay them into the 17 primary indexes on a four-thread pool, and build the class hierarchy. The
 * legacy Lucene dictionary cannot be loaded next to the ported kernel's Lucene 9, so the ported
 * {@code ProjectContextFactory.create} is timed on its own, with and without an existing Lucene index. Runs alternate
 * after a warm-up, each after a full garbage collection, and the medians are compared. The results, with the time of
 * each phase, go to
 * {@code target/perf/P0-baseline.md}; docs/perf records them.
 */
public class ProjectLoadBaselineIT {

    private static final int CLASS_COUNT = 10_000;

    private static final int AXIOM_COUNT = 50_000;

    private static final int WARM_UP_RUNS = 5;

    private static final int MEASURED_RUNS = 9;

    private static final double MAX_RATIO = 1.2;

    @TempDir
    Path dataDirectory;

    @Test
    public void shouldLoadFiftyThousandAxiomProjectWithinBaseline() throws Exception {
        var projectId = UUID.randomUUID().toString();
        var revisions = CompatDatasets.generated(CLASS_COUNT);
        assertThat(revisions.get(0).changes().size(), is(AXIOM_COUNT));
        LegacyKernel.writeRevisions(dataDirectory, projectId, revisions);

        var pool = Executors.newFixedThreadPool(4);
        var legacyLoads = new ArrayList<Load>();
        var portedLoads = new ArrayList<Load>();
        try {
            for(int i = 0; i < WARM_UP_RUNS; i++) {
                loadLegacy(projectId, pool);
                loadPorted(projectId, pool);
            }
            for(int i = 0; i < MEASURED_RUNS; i++) {
                legacyLoads.add(loadLegacy(projectId, pool));
                portedLoads.add(loadPorted(projectId, pool));
            }
        } finally {
            pool.shutdownNow();
        }

        long contextFirstOpen;
        var contextReopenTimes = new ArrayList<Long>();
        try(var kernel = new ProjectKernelFixture(dataDirectory)) {
            contextFirstOpen = timeOpen(kernel, projectId);
            for(int i = 0; i < 3; i++) {
                contextReopenTimes.add(timeOpen(kernel, projectId));
            }
        }

        var ratio = (double) median(portedLoads, Load::totalMillis) / median(legacyLoads, Load::totalMillis);
        writeReport(legacyLoads, portedLoads, ratio, contextFirstOpen, contextReopenTimes);
        assertThat("ported/legacy load time ratio (see target/perf/P0-baseline.md)",
                   ratio,
                   is(lessThanOrEqualTo(MAX_RATIO)));
    }

    /**
     * Collects the previous load's indexes first, so that their garbage is not charged to the next load.
     */
    private static void collectGarbage() {
        System.gc();
        System.gc();
    }

    private Load loadLegacy(String projectId, ExecutorService pool) {
        collectGarbage();
        var start = System.nanoTime();
        var legacy = LegacyKernel.load(dataDirectory, projectId, pool);
        var hierarchyStart = System.nanoTime();
        var roots = legacy.classHierarchy.getChildren(new OWLDataFactoryImpl().getOWLThing());
        var end = System.nanoTime();
        assertThat(roots.isEmpty(), is(false));
        legacy.dispose();
        return new Load(millis(end - start),
                        millis(legacy.revisionsNanos),
                        millis(legacy.indexesNanos),
                        millis(end - hierarchyStart));
    }

    private Load loadPorted(String projectId, ExecutorService pool) {
        collectGarbage();
        var start = System.nanoTime();
        var dataFactory = new OWLDataFactoryImpl();
        var portedProjectId = ProjectId.get(projectId);
        var store = new RevisionStoreFactory(new DataDirectoryLayout(dataDirectory).getChangeHistoryFileFactory(),
                                             dataFactory,
                                             new OntologyChangeRecordTranslatorImpl(),
                                             pool).createRevisionStore(portedProjectId);
        var revisionManager = new RevisionManager(store);
        var revisionsLoaded = System.nanoTime();
        var indexes = ProjectIndexes.builder(dataFactory);
        new IndexUpdater(revisionManager, indexes.updatable(), pool, portedProjectId).buildIndexes();
        indexes.get(org.industrial.ontology.kernel.index.ProjectOntologiesIndex.class).init(revisionManager);
        var hierarchyStart = System.nanoTime();
        var classHierarchy = new ClassHierarchyProvider(portedProjectId,
                                                        dataFactory.getOWLThing(),
                                                        indexes.get(ProjectOntologiesIndex.class),
                                                        indexes.get(SubClassOfAxiomsBySubClassIndex.class),
                                                        indexes.get(EquivalentClassesAxiomsIndex.class),
                                                        indexes.get(ProjectSignatureByTypeIndex.class),
                                                        indexes.get(AxiomsByEntityReferenceIndex.class),
                                                        indexes.get(EntitiesInProjectSignatureByIriIndex.class));
        var roots = classHierarchy.getChildren(dataFactory.getOWLThing());
        var end = System.nanoTime();
        assertThat(roots.isEmpty(), is(false));
        store.dispose();
        return new Load(millis(end - start),
                        millis(revisionsLoaded - start),
                        millis(hierarchyStart - revisionsLoaded),
                        millis(end - hierarchyStart));
    }

    private static long timeOpen(ProjectKernelFixture kernel, String projectId) {
        collectGarbage();
        var start = System.nanoTime();
        try(var context = kernel.open(ProjectId.get(projectId))) {
            context.hierarchies().classHierarchy().getChildren(context.dataFactory().getOWLThing());
            return millis(System.nanoTime() - start);
        }
    }

    private void writeReport(List<Load> legacyLoads,
                             List<Load> portedLoads,
                             double ratio,
                             long contextFirstOpen,
                             List<Long> contextReopenTimes) throws Exception {
        var lines = List.of(
                "# P0 load-time baseline",
                "",
                "- Project: " + AXIOM_COUNT + " axioms (" + CLASS_COUNT + " classes), one revision written by the "
                        + "legacy store",
                "- JVM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version")
                        + ", " + Runtime.getRuntime().availableProcessors() + " processors, "
                        + System.getProperty("os.name"),
                "- Runs: " + WARM_UP_RUNS + " warm-up, " + MEASURED_RUNS + " measured, alternating; medians in ms",
                "",
                "| Load | Total | Read revisions | Replay into indexes | Class hierarchy |",
                "|---|---|---|---|---|",
                row("Legacy", legacyLoads),
                row("Ported", portedLoads),
                "",
                "| Ported ProjectContextFactory.create | Median (ms) |",
                "|---|---|",
                "| Existing Lucene index | " + median(contextReopenTimes, Long::longValue) + " |",
                "| Building the Lucene index (first load) | " + contextFirstOpen + " |",
                "",
                String.format("Ported / legacy total: %.2f (limit %.1f)", ratio, MAX_RATIO));
        var report = Path.of("target", "perf", "P0-baseline.md");
        Files.createDirectories(report.getParent());
        Files.write(report, lines, StandardCharsets.UTF_8);
    }

    private static String row(String name, List<Load> loads) {
        return "| " + name
                + " | " + median(loads, Load::totalMillis)
                + " | " + median(loads, Load::revisionsMillis)
                + " | " + median(loads, Load::indexesMillis)
                + " | " + median(loads, Load::hierarchyMillis) + " |";
    }

    private static <T> long median(List<T> loads, ToLongFunction<T> time) {
        var sorted = loads.stream().map(time::applyAsLong).sorted().collect(toList());
        return sorted.get(sorted.size() / 2);
    }

    private static long millis(long nanos) {
        return nanos / 1_000_000;
    }

    private record Load(long totalMillis, long revisionsMillis, long indexesMillis, long hierarchyMillis) {
    }
}
