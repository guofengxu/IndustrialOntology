package org.industrial.ontology.kernel.owlapi.change;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.revision.RevisionManager;
import org.industrial.ontology.kernel.revision.RevisionStore;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;
import org.industrial.ontology.domain.core.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.change.RevisionManagerImpl_TestCase}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RevisionManagerTest {

    public static final String HIGHLEVEL_DESC = "HIGHLEVEL_DESC";

    public static final long TIME_STAMP = 33l;

    private RevisionManager manager;

    @Mock
    private RevisionStore revisionStore;

    @Mock
    private UserId userId;

    @Mock
    private Revision revision;

    @Mock
    private RevisionNumber revisionNumber, nextRevisionNumber;

    @Mock
    private ImmutableList<OntologyChange> changes;

    @BeforeEach
    public void setUp() throws Exception {
        manager = new RevisionManager(revisionStore);
        when(revisionStore.getHead()).thenReturn(revisionNumber);
        when(revisionStore.getRevision(revisionNumber)).thenReturn(java.util.Optional.of(revision));
        when(revisionStore.getRevisions()).thenReturn(ImmutableList.of(revision));
        when(revisionNumber.getNextRevisionNumber()).thenReturn(nextRevisionNumber);
        when(revision.getUserId()).thenReturn(userId);
        when(revision.getHighLevelDescription()).thenReturn(HIGHLEVEL_DESC);
        when(revision.getSize()).thenReturn(1);
        when(revision.getTimestamp()).thenReturn(TIME_STAMP);
        when(revision.getRevisionNumber()).thenReturn(revisionNumber);
    }

    @Test
    public void shouldThrowNullPointerExceptionIf_revisionStore_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new RevisionManager(null);
        });
    }

    @Test
    public void should_getCurrentRevision() {
        assertThat(manager.getCurrentRevision(), is(revisionNumber));
    }

    @Test
    public void should_getRevisions() {
        assertThat(manager.getRevisions(), is(Collections.singletonList(revision)));
    }

    @Test
    public void should_getRevision() {
        assertThat(manager.getRevision(revisionNumber), is(Optional.of(revision)));
    }

    @Test
    public void should_getRevisionSummary() {
        java.util.Optional<RevisionSummary> summary = manager.getRevisionSummary(revisionNumber);
        assertThat(summary.isPresent(), is(true));
    }

    @Test
    public void should_getRevisionSummaryWithCorrectUserId() {
        java.util.Optional<RevisionSummary> summary = manager.getRevisionSummary(revisionNumber);
        RevisionSummary revisionSummary = summary.get();
        assertThat(revisionSummary.getUserId(), is(userId));
    }

    @Test
    public void should_getRevisionSummaryWithCorrectRevisionNumber() {
        java.util.Optional<RevisionSummary> summary = manager.getRevisionSummary(revisionNumber);
        RevisionSummary revisionSummary = summary.get();
        assertThat(revisionSummary.getRevisionNumber(), is(revisionNumber));
    }

    @Test
    public void should_getRevisionSummaryWithCorrectChangeCount() {
        java.util.Optional<RevisionSummary> summary = manager.getRevisionSummary(revisionNumber);
        RevisionSummary revisionSummary = summary.get();
        assertThat(revisionSummary.getChangeCount(), is(1));
    }

    @Test
    public void should_getRevisionSummaryWithCorrectTimestamp() {
        java.util.Optional<RevisionSummary> summary = manager.getRevisionSummary(revisionNumber);
        RevisionSummary revisionSummary = summary.get();
        assertThat(revisionSummary.getTimestamp(), is(TIME_STAMP));
    }

    @Test
    public void should_getRevisionSummaries() {
        assertThat(manager.getRevisionSummaries(), hasSize(1));
    }

    @Test
    public void should_addRevision() {
        UserId userId = mock(UserId.class);
        List<OntologyChange> changes = Collections.singletonList(AddAxiomChange.of(new OWLOntologyID(), mock(OWLAxiom.class)));
        manager.addRevision(userId, changes, HIGHLEVEL_DESC);
        ArgumentCaptor<Revision> revisionCaptor = ArgumentCaptor.forClass(Revision.class);
        verify(revisionStore, times(1)).append(revisionCaptor.capture());
        Revision addedRevision = revisionCaptor.getValue();
        assertThat(addedRevision.getUserId(), is(userId));
        assertThat(addedRevision.getHighLevelDescription(), is(HIGHLEVEL_DESC));
        assertThat(addedRevision.getRevisionNumber(), is(nextRevisionNumber));
    }
}
