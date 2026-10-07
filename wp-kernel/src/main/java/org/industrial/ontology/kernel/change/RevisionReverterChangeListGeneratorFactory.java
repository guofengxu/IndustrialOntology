package org.industrial.ontology.kernel.change;

import org.industrial.ontology.kernel.api.revision.RevisionManager;
import org.industrial.ontology.domain.revision.RevisionNumber;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link RevisionReverterChangeListGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.RevisionReverterChangeListGeneratorFactory} (generated in the legacy build).
 */
public final class RevisionReverterChangeListGeneratorFactory {

    private final Supplier<RevisionManager> revisionManager;

    public RevisionReverterChangeListGeneratorFactory(Supplier<RevisionManager> revisionManager) {
        this.revisionManager = java.util.Objects.requireNonNull(revisionManager);
    }

    public RevisionReverterChangeListGenerator create(@Nonnull RevisionNumber revisionNumber) {
        return new RevisionReverterChangeListGenerator(revisionNumber, revisionManager.get());
    }
}
