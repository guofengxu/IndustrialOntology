package org.industrial.ontology.kernel.project;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.api.index.ProjectOntologiesIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.semanticweb.owlapi.model.OWLClass;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ProjectContextFactory} loads a project from its directory and {@link ProjectContext#close()} releases it
 * (docs/01 §2, §3.4): a reopened project sees the revisions, hierarchy and Lucene index written before it was closed,
 * a missing Lucene index is rebuilt, the shared thread pools survive the close, and the context's lock is the one
 * {@code ChangeManager} writes under.
 */
public class ProjectContextFactoryIT {

    @TempDir
    Path dataDirectory;

    private ProjectKernelFixture kernel;

    private ProjectId projectId;

    @BeforeEach
    public void setUp() {
        kernel = new ProjectKernelFixture(dataDirectory);
        projectId = ProjectKernelFixture.freshProjectId();
    }

    @AfterEach
    public void tearDown() throws Exception {
        kernel.close();
    }

    @Test
    public void shouldOpenEmptyProject() {
        try(var context = kernel.open(projectId)) {
            assertThat(context.revisionManager().getRevisions(), is(empty()));
            assertThat(context.indexes().get(ProjectOntologiesIndex.class).getOntologyIds().count(), is(0L));
            assertThat(context.hierarchies().classHierarchy().getChildren(context.dataFactory().getOWLThing()),
                       is(empty()));
            assertThat(Files.isDirectory(luceneDirectory()), is(true));
        }
    }

    @Test
    public void shouldReloadRevisionsIndexesAndSearchIndexWrittenBeforeClose() {
        OWLClass pizza;
        OWLClass margherita;
        try(var context = kernel.open(projectId)) {
            pizza = ProjectKernelFixture.createClass(context, "Pizza", ImmutableSet.of());
            margherita = ProjectKernelFixture.createClass(context, "Margherita", ImmutableSet.of(pizza));
        }

        try(var reopened = kernel.open(projectId)) {
            assertThat(reopened.revisionManager().getRevisions().size(), is(2));
            assertThat(reopened.hierarchies().classHierarchy().getChildren(pizza), contains(margherita));
            assertThat(reopened.dictionary().getShortForm(margherita), is("Margherita"));
            assertThat(ProjectKernelFixture.searchClasses(reopened, "marg"), contains(margherita));
        }
    }

    @Test
    public void shouldRebuildMissingLuceneIndexWhenLoading() throws Exception {
        OWLClass pizza;
        try(var context = kernel.open(projectId)) {
            pizza = ProjectKernelFixture.createClass(context, "Pizza", ImmutableSet.of());
        }
        FileUtils.deleteDirectory(luceneDirectory().toFile());

        try(var reopened = kernel.open(projectId)) {
            assertThat(ProjectKernelFixture.searchClasses(reopened, "piz"), contains(pizza));
        }
    }

    @Test
    public void shouldReleaseProjectResourcesButKeepSharedThreadPools() {
        var context = kernel.open(projectId);
        context.close();
        context.close();

        assertThat(context.isClosed(), is(true));
        var threadPools = kernel.threadPools();
        assertThat(threadPools.indexUpdates().isShutdown(), is(false));
        assertThat(threadPools.revisionWrites().isShutdown(), is(false));
        assertThat(threadPools.eventPurges().isShutdown(), is(false));
        // The Lucene write lock was released, so the project opens again.
        kernel.open(projectId).close();
    }

    @Test
    public void shouldReleaseWhatWasOpenedWhenLoadingFails() {
        var context = kernel.open(projectId);
        ProjectKernelFixture.createClass(context, "Pizza", ImmutableSet.of());
        // A second context cannot take the Lucene write lock while the first one is open.
        assertThrows(RuntimeException.class, () -> kernel.open(projectId));
        context.close();

        try(var reopened = kernel.open(projectId)) {
            assertThat(reopened.revisionManager().getRevisions().size(), is(1));
        }
    }

    @Test
    public void shouldApplyChangesUnderTheContextLock() throws Exception {
        var writer = Executors.newSingleThreadExecutor();
        try(var context = kernel.open(projectId)) {
            Future<OWLClass> write;
            var readLock = context.lock().readLock();
            readLock.lock();
            try {
                write = writer.submit(() -> ProjectKernelFixture.createClass(context, "Pizza", ImmutableSet.of()));
                assertThrows(TimeoutException.class, () -> write.get(300, TimeUnit.MILLISECONDS));
                assertThat(context.revisionManager().getRevisions(), is(empty()));
            } finally {
                readLock.unlock();
            }
            write.get(30, TimeUnit.SECONDS);
            assertThat(context.revisionManager().getRevisions().size(), is(1));
        } finally {
            writer.shutdownNow();
        }
    }

    private Path luceneDirectory() {
        return kernel.dataDirectoryLayout().getLuceneDirectoryPathSupplier(projectId).get();
    }
}
