package org.industrial.ontology.kernel.io.merge;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.merge.OntologyDiff;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.change.HasApplyChanges;
import org.industrial.ontology.kernel.diff.OntologyDiff2OntologyChanges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.OntologyPatcher_TestCase}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-21
 * <p>
 * The legacy test supplied the user through a mocked dispatch {@code ExecutionContext}; the port passes the
 * {@link UserId} directly, as {@link OntologyPatcher#applyPatch} now takes it.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OntologyPatcherTest {

    private OntologyPatcher patcher;

    @Mock
    private HasApplyChanges changeManager;

    @Mock
    private OntologyDiff2OntologyChanges ontologyDiff2OntologyChanges;

    @Mock
    private OntologyDiff ontologyDiff;

    private String commitMessage = "The Commit Message";

    @Mock
    private OntologyChange ontologyChange;

    @Mock
    private UserId userId;

    @Captor
    private ArgumentCaptor<UserId> userIdCaptor;


    @Captor
    private ArgumentCaptor<ChangeListGenerator<?>> changeListGeneratorCaptor;

    @BeforeEach
    public void setUp() {
        patcher = new OntologyPatcher(changeManager,
                                      ontologyDiff2OntologyChanges);
        when(ontologyDiff2OntologyChanges.getOntologyChangesFromDiff(ontologyDiff))
                .thenReturn(ImmutableList.of(ontologyChange));
    }

    @Test
    public void shouldApplyChangesWithSuppliedUser() {
        patcher.applyPatch(Collections.singleton(ontologyDiff),
                           commitMessage,
                           userId);

        verify(changeManager, times(1)).applyChanges(userIdCaptor.capture(), changeListGeneratorCaptor.capture());
        assertThat(userIdCaptor.getValue(), is(userId));
    }

    @Test
    public void shouldCreateChangeGeneratorForOntologyChanges() {
        patcher.applyPatch(Collections.singleton(ontologyDiff),
                           commitMessage,
                           userId);

        verify(changeManager, times(1)).applyChanges(userIdCaptor.capture(), changeListGeneratorCaptor.capture());

        var changeListGenerator = changeListGeneratorCaptor.getValue();
        var changes = changeListGenerator.generateChanges(new ChangeGenerationContext(userId));
        assertThat(changes.getChanges(), hasItem(ontologyChange));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCreateChangeGeneratorWithSuppliedMessage() {
        patcher.applyPatch(Collections.singleton(ontologyDiff),
                           commitMessage,
                           userId);

        verify(changeManager, times(1)).applyChanges(userIdCaptor.capture(), changeListGeneratorCaptor.capture());

        var changeListGenerator = changeListGeneratorCaptor.getValue();
        var message = changeListGenerator.getMessage(mock(ChangeApplicationResult.class));
        assertThat(message, is(this.commitMessage));


    }
}
