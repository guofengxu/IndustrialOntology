package org.industrial.ontology.kernel.io.merge;

import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.merge.OntologyDiff;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;
import org.industrial.ontology.kernel.change.ChangeApplicationResult;
import org.industrial.ontology.kernel.change.ChangeGenerationContext;
import org.industrial.ontology.kernel.change.ChangeListGenerator;
import org.industrial.ontology.kernel.change.HasApplyChanges;
import org.industrial.ontology.kernel.diff.OntologyDiff2OntologyChanges;
import org.industrial.ontology.kernel.owlapi.RenameMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.merge.OntologyPatcher}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 * <p>
 * The legacy {@code ComputeProjectMergeActionHandler} and {@code MergeUploadedProjectActionHandler} that drove
 * this patcher were not ported (the dispatch layer is not ported, docs/01 §4); they will be replaced by
 * {@code wp-app} services in P4. {@link #applyPatch} takes the {@link UserId} that it used to read from the
 * dispatch {@code ExecutionContext}.
 */
public class OntologyPatcher {

    @Nonnull
    private final HasApplyChanges changeManager;

    @Nonnull
    private final OntologyDiff2OntologyChanges ontologyDiff2OntologyChanges;

    public OntologyPatcher(@Nonnull HasApplyChanges changeManager,
                           @Nonnull OntologyDiff2OntologyChanges ontologyDiff2OntologyChanges) {
        this.changeManager = checkNotNull(changeManager);
        this.ontologyDiff2OntologyChanges = checkNotNull(ontologyDiff2OntologyChanges);
    }

    public void applyPatch(@Nonnull Collection<OntologyDiff> diffSet,
                           @Nonnull String commitMessage,
                           @Nonnull UserId userId) {
        var changeList = new ArrayList<OntologyChange>();
        for(OntologyDiff diff : diffSet) {
            List<OntologyChange> changes = ontologyDiff2OntologyChanges.getOntologyChangesFromDiff(diff);
            changeList.addAll(changes);
        }
        applyChanges(commitMessage, changeList, userId);
    }

    private void applyChanges(String commitMessage,
                              final List<OntologyChange> changes,
                              UserId userId) {
        changeManager.applyChanges(userId, new ChangeListGenerator<Boolean>() {
            @Override
            public OntologyChangeList<Boolean> generateChanges(ChangeGenerationContext context) {
                OntologyChangeList.Builder<Boolean> builder = OntologyChangeList.builder();
                builder.addAll(changes);
                return builder.build(!changes.isEmpty());
            }

            @Override
            public Boolean getRenamedResult(Boolean result, RenameMap renameMap) {
                return true;
            }

            @Nonnull
            @Override
            public String getMessage(ChangeApplicationResult<Boolean> result) {
                return commitMessage;
            }
        });

    }
}
