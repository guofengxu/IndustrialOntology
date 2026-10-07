package org.industrial.ontology.kernel.change;



import org.industrial.ontology.kernel.owlapi.RenameMap;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.industrial.ontology.domain.revision.RevisionNumber;
import javax.annotation.Nonnull;
import java.util.ArrayList;

import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.OntologyChangeList;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.RevisionReverterChangeListGenerator}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19/03/15
 */
public class RevisionReverterChangeListGenerator implements ChangeListGenerator<Boolean> {

    @Nonnull
    private final RevisionNumber revisionNumber;

    @Nonnull
    private final RevisionManager revisionManager;

    public RevisionReverterChangeListGenerator(@Nonnull RevisionNumber revisionNumber,
                                               @Nonnull RevisionManager revisionManager) {
        this.revisionNumber = checkNotNull(revisionNumber);
        this.revisionManager = checkNotNull(revisionManager);
    }

    @Override
    public Boolean getRenamedResult(Boolean result, RenameMap renameMap) {
        return result;
    }

    @Override
    public OntologyChangeList<Boolean> generateChanges(ChangeGenerationContext context) {
        Optional<Revision> revision = revisionManager.getRevision(revisionNumber);
        if(revision.isEmpty()) {
            return OntologyChangeList.<Boolean>builder().build(false);
        }
        var changes = new ArrayList<OntologyChange>();
        var theRevision = revision.get();
        for(OntologyChange change : theRevision.getChanges()) {
            var inverseChange = change.getInverseChange();
            changes.add(0, inverseChange);
        }
        return OntologyChangeList.<Boolean>builder().addAll(changes).build(true);
    }

    @Nonnull
    @Override
    public String getMessage(ChangeApplicationResult<Boolean> result) {
        return "Reverted revision " + revisionNumber.getValue();
    }
}
