package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.project.PrefixDeclarations;

import javax.annotation.Nonnull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.PrefixDeclarationsStore}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 */
public interface PrefixDeclarationsStore {

    PrefixDeclarations find(@Nonnull ProjectId projectId);
}
