package org.industrial.ontology.kernel.change;

import org.industrial.ontology.kernel.entity.EntityRenamer;
import org.industrial.ontology.kernel.api.index.ProjectSignatureIndex;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link FindAndReplaceIRIPrefixChangeGenerator}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.change.FindAndReplaceIRIPrefixChangeGeneratorFactory} (generated in the legacy build).
 */
public final class FindAndReplaceIRIPrefixChangeGeneratorFactory {

    private final Supplier<ProjectSignatureIndex> projectSignatureIndex;

    private final Supplier<EntityRenamer> entityRenamer;

    public FindAndReplaceIRIPrefixChangeGeneratorFactory(Supplier<ProjectSignatureIndex> projectSignatureIndex,
            Supplier<EntityRenamer> entityRenamer) {
        this.projectSignatureIndex = java.util.Objects.requireNonNull(projectSignatureIndex);
        this.entityRenamer = java.util.Objects.requireNonNull(entityRenamer);
    }

    public FindAndReplaceIRIPrefixChangeGenerator create(@Nonnull String fromPrefix, @Nonnull String toPrefix) {
        return new FindAndReplaceIRIPrefixChangeGenerator(fromPrefix, toPrefix, projectSignatureIndex.get(), entityRenamer.get());
    }
}
