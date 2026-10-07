package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.issues.EntityDiscussionThreadRepository}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 */
public interface EntityDiscussionThreadRepository {

    int getOpenCommentsCount(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity);

    /** Re-points discussion threads at {@code withEntity}, used when entities are merged. */
    void replaceEntity(@Nonnull ProjectId projectId, @Nonnull OWLEntity entity, @Nonnull OWLEntity withEntity);
}
