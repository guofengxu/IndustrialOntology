package org.industrial.ontology.compat;

import com.google.common.collect.ImmutableSet;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.kernel.project.PizzaOntology;
import org.industrial.ontology.kernel.project.ProjectKernelFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Acceptance test of P0-03 (docs/05): the BinaryOWL change history is shared with the legacy kernel in both
 * directions. Revisions written by the legacy {@code RevisionStoreImpl} load into the ported store with the same
 * head revision number as the legacy {@code GetHeadRevisionNumber} path and the same number, author, timestamp,
 * description and changes; revisions appended through the ported {@code ChangeManager} are read back by the legacy
 * store.
 */
public class LegacyRevisionCompatibilityIT {

    @TempDir
    Path dataDirectory;

    @TempDir
    Path sources;

    @Test
    public void shouldLoadPizzaImportWrittenByLegacyStore() throws Exception {
        assertLoadsLegacyRevisions(CompatDatasets.pizza(PizzaOntology.copyTo(sources)));
    }

    @Test
    public void shouldLoadEditHistoryWrittenByLegacyStore() throws Exception {
        assertLoadsLegacyRevisions(CompatDatasets.edited(PizzaOntology.copyTo(sources), 150, 7L));
    }

    @Test
    public void shouldWriteRevisionsThatLegacyStoreReads() throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory, projectId, CompatDatasets.pizza(PizzaOntology.copyTo(sources)));

        List<NeutralChange> appended;
        try(var kernel = new ProjectKernelFixture(dataDirectory);
            var context = kernel.open(ProjectId.get(projectId))) {
            var pizza = PizzaOntology.cls(context.dataFactory(), "Pizza");
            ProjectKernelFixture.createClass(context, "Pizza Bianca", ImmutableSet.of(pizza));
            ProjectKernelFixture.createClass(context, "Calzone", ImmutableSet.of(pizza));
            appended = context.revisionManager()
                              .getRevisions()
                              .subList(1, 3)
                              .stream()
                              .flatMap(revision -> revision.getChanges().stream())
                              .map(NeutralChange::fromKernel)
                              .collect(toList());
        }

        var legacyRevisions = LegacyKernel.readRevisions(dataDirectory, projectId);
        assertThat(legacyRevisions.size(), is(3));
        assertThat(LegacyKernel.headRevisionNumber(dataDirectory, projectId), is(3L));
        assertThat(legacyRevisions.get(2).getUserId().getUserName(), is(ProjectKernelFixture.USER.getUserName()));
        assertThat(legacyRevisions.subList(1, 3)
                                  .stream()
                                  .flatMap(revision -> revision.getChanges().stream())
                                  .map(NeutralChange::fromLegacy)
                                  .collect(toList()),
                   is(appended));
    }

    private void assertLoadsLegacyRevisions(List<CompatDatasets.RevisionSpec> written) throws Exception {
        var projectId = UUID.randomUUID().toString();
        LegacyKernel.writeRevisions(dataDirectory, projectId, written);
        var legacyRevisions = LegacyKernel.readRevisions(dataDirectory, projectId);
        var legacyHead = LegacyKernel.headRevisionNumber(dataDirectory, projectId);

        try(var kernel = new ProjectKernelFixture(dataDirectory);
            var context = kernel.open(ProjectId.get(projectId))) {
            var revisions = context.revisionManager().getRevisions();
            assertThat(legacyHead, is((long) written.size()));
            assertThat(context.revisionManager().getCurrentRevision().getValue(), is(legacyHead));
            assertThat(revisions.size(), is(legacyRevisions.size()));
            for(int i = 0; i < revisions.size(); i++) {
                var revision = revisions.get(i);
                var legacyRevision = legacyRevisions.get(i);
                assertThat(revision.getRevisionNumber().getValue(), is(legacyRevision.getRevisionNumber().getValue()));
                assertThat(revision.getUserId().getUserName(), is(legacyRevision.getUserId().getUserName()));
                assertThat(revision.getTimestamp(), is(legacyRevision.getTimestamp()));
                assertThat(revision.getHighLevelDescription(), is(legacyRevision.getHighLevelDescription()));
                var changes = revision.getChanges().stream().map(NeutralChange::fromKernel).collect(toList());
                assertThat(changes, is(legacyRevision.getChanges()
                                                     .stream()
                                                     .map(NeutralChange::fromLegacy)
                                                     .collect(toList())));
                assertThat(changes, is(written.get(i).changes()));
            }
        }
    }
}
