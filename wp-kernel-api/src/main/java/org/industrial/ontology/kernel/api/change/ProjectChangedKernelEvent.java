package org.industrial.ontology.kernel.api.change;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.revision.RevisionNumber;

import java.util.Objects;

/**
 * Published by {@code ChangeManager} after a revision has been committed and its high-level events posted.
 * <p>
 * The legacy {@code ChangeManager} updated {@code ProjectDetails.modifiedAt} and called the project webhook
 * itself; both are application concerns, so wp-app subscribes to this event instead (docs/01 §3.1). The payload
 * matches the legacy webhook payload {@code {projectId, userId, revisionNumber, timestamp}} (docs/01 §5.5).
 */
public record ProjectChangedKernelEvent(ProjectId projectId, UserId userId, RevisionNumber revisionNumber,
                                        long timestamp) {

    public ProjectChangedKernelEvent {
        Objects.requireNonNull(projectId, "projectId");
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(revisionNumber, "revisionNumber");
    }
}
