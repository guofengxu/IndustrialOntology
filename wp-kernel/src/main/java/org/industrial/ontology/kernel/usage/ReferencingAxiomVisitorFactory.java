package org.industrial.ontology.kernel.usage;

import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.kernel.render.RenderingManager;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLEntity;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ReferencingAxiomVisitor}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.usage.ReferencingAxiomVisitorFactory} (generated in the legacy build).
 */
public final class ReferencingAxiomVisitorFactory {

    private final Supplier<RenderingManager> renderingManager;

    private final Supplier<EntitiesInProjectSignatureByIriIndex> entitiesInSignatureIndex;

    public ReferencingAxiomVisitorFactory(Supplier<RenderingManager> renderingManager,
            Supplier<EntitiesInProjectSignatureByIriIndex> entitiesInSignatureIndex) {
        this.renderingManager = java.util.Objects.requireNonNull(renderingManager);
        this.entitiesInSignatureIndex = java.util.Objects.requireNonNull(entitiesInSignatureIndex);
    }

    public ReferencingAxiomVisitor create(@Nonnull OWLEntity usageOf) {
        return new ReferencingAxiomVisitor(usageOf, renderingManager.get(), entitiesInSignatureIndex.get());
    }
}
