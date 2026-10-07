package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.watches.Watch;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.watches.WatchManager}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 * One instance serves one project.
 */
public interface WatchManager {

    Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity);

    Set<Watch> getDirectWatches(@Nonnull OWLEntity watchedEntity, @Nonnull UserId userId);
}
