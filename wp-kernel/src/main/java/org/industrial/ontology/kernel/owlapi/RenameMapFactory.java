package org.industrial.ontology.kernel.owlapi;

import org.industrial.ontology.kernel.render.RenderingManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link RenameMap}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.owlapi.RenameMapFactory} (generated in the legacy build).
 */
public final class RenameMapFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<RenderingManager> renderingManager;

    public RenameMapFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<RenderingManager> renderingManager) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.renderingManager = java.util.Objects.requireNonNull(renderingManager);
    }

    public RenameMap create(@Nonnull Map<IRI, IRI> map) {
        return new RenameMap(map, dataFactory.get(), renderingManager.get());
    }
}
