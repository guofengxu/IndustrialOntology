package org.industrial.ontology.kernel.index;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.index.DependentIndex;
import org.industrial.ontology.kernel.api.index.Index;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the dependency layering of {@link IndexUpdater}: an index is only fed changes once every index it depends
 * on has finished, and a layer starts only after the previous layer has finished. The legacy repository has no test
 * for this, so the expectations come from the ranking in {@code IndexUpdater} (rank = length of the longest
 * dependency path).
 */
public class IndexUpdaterTest {

    private final AtomicInteger clock = new AtomicInteger();

    private ExecutorService executor;

    private ImmutableList<OntologyChange> firstChanges;

    private ImmutableList<OntologyChange> secondChanges;

    // b -> a, c -> b, e -> {a, d}: ranks a = d = 0, b = e = 1, c = 2
    private RecordingIndex a;

    private RecordingIndex b;

    private RecordingIndex c;

    private RecordingIndex d;

    private RecordingIndex e;

    private List<RecordingIndex> all;

    private IndexUpdater indexUpdater;

    @BeforeEach
    public void setUp() {
        executor = Executors.newFixedThreadPool(4);
        a = new RecordingIndex("a");
        b = new RecordingIndex("b", a);
        c = new RecordingIndex("c", b);
        d = new RecordingIndex("d");
        e = new RecordingIndex("e", a, d);
        all = List.of(a, b, c, d, e);
        var dataFactory = new OWLDataFactoryImpl();
        var ontologyId = new OWLOntologyID(IRI.create("http://example.org/ont"));
        firstChanges = ImmutableList.of(AddAxiomChange.of(ontologyId, dataFactory.getOWLDeclarationAxiom(
                dataFactory.getOWLClass(IRI.create("http://example.org/A")))));
        secondChanges = ImmutableList.of(AddAxiomChange.of(ontologyId, dataFactory.getOWLDeclarationAxiom(
                dataFactory.getOWLClass(IRI.create("http://example.org/B")))));
        var revisionManager = mock(RevisionManager.class);
        var userId = UserId.getUserId("The User");
        when(revisionManager.getRevisions()).thenReturn(ImmutableList.of(
                new Revision(userId, RevisionNumber.getRevisionNumber(1), firstChanges, 1L, "first"),
                new Revision(userId, RevisionNumber.getRevisionNumber(2), secondChanges, 2L, "second")));
        // Dependants come before their dependencies so that insertion order cannot explain a correct result.
        var indexes = new LinkedHashSet<UpdatableIndex>(List.of(c, e, b, d, a));
        indexUpdater = new IndexUpdater(revisionManager, indexes, executor,
                                        ProjectId.get(UUID.randomUUID().toString()));
    }

    @AfterEach
    public void tearDown() {
        executor.shutdownNow();
    }

    @Test
    public void shouldBuildEachIndexOnlyAfterItsDependencies() {
        indexUpdater.buildIndexes();
        assertFinishedBefore(a, b);
        assertFinishedBefore(b, c);
        assertFinishedBefore(a, e);
        assertFinishedBefore(d, e);
    }

    @Test
    public void shouldFinishEachLayerBeforeStartingTheNext() {
        indexUpdater.buildIndexes();
        // d and b, and e and c, have no dependency between them; only the layering orders them.
        assertFinishedBefore(d, b);
        assertFinishedBefore(e, c);
    }

    @Test
    public void shouldReplayAllRevisionsInOrder() {
        indexUpdater.buildIndexes();
        for(var index : all) {
            assertThat(index + " changes", index.appliedChanges(), contains(firstChanges, secondChanges));
        }
    }

    @Test
    public void shouldBuildIndexesOnlyOnce() {
        indexUpdater.buildIndexes();
        indexUpdater.buildIndexes();
        for(var index : all) {
            assertThat(index + " changes", index.appliedChanges().size(), is(2));
        }
    }

    @Test
    public void shouldApplyIncrementalChangesInDependencyOrder() {
        indexUpdater.buildIndexes();
        all.forEach(RecordingIndex::clearTicks);
        indexUpdater.updateIndexes(secondChanges);
        for(var index : all) {
            assertThat(index + " changes", index.appliedChanges(), contains(firstChanges, secondChanges, secondChanges));
        }
        assertFinishedBefore(a, b);
        assertFinishedBefore(b, c);
        assertFinishedBefore(d, e);
        assertFinishedBefore(e, c);
    }

    private static void assertFinishedBefore(RecordingIndex dependency, RecordingIndex dependant) {
        assertThat(dependency + " must finish before " + dependant + " starts",
                   dependency.lastEnd(), lessThan(dependant.firstStart()));
    }

    /** Records when it was fed changes, using a logical clock shared by all indexes of a test. */
    private class RecordingIndex implements UpdatableIndex, DependentIndex {

        private final String name;

        private final List<Index> dependencies;

        private final List<ImmutableList<OntologyChange>> appliedChanges = new ArrayList<>();

        private final List<Integer> starts = new ArrayList<>();

        private final List<Integer> ends = new ArrayList<>();

        RecordingIndex(String name, Index... dependencies) {
            this.name = name;
            this.dependencies = List.of(dependencies);
        }

        @Override
        public synchronized void applyChanges(@Nonnull ImmutableList<OntologyChange> changes) {
            starts.add(clock.incrementAndGet());
            appliedChanges.add(changes);
            Thread.yield();
            ends.add(clock.incrementAndGet());
        }

        @Nonnull
        @Override
        public Collection<Index> getDependencies() {
            return dependencies;
        }

        synchronized List<ImmutableList<OntologyChange>> appliedChanges() {
            return List.copyOf(appliedChanges);
        }

        synchronized int firstStart() {
            return Collections.min(starts);
        }

        synchronized int lastEnd() {
            return Collections.max(ends);
        }

        synchronized void clearTicks() {
            starts.clear();
            ends.clear();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
