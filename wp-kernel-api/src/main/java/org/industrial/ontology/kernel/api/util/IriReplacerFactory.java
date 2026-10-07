package org.industrial.ontology.kernel.api.util;

import com.google.common.collect.ImmutableMap;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link IriReplacer}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.util.IriReplacerFactory} (generated in the legacy build).
 */
public final class IriReplacerFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    public IriReplacerFactory(Supplier<OWLDataFactory> dataFactory) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
    }

    public IriReplacer create(@Nonnull ImmutableMap<IRI, IRI> iriMap) {
        return new IriReplacer(dataFactory.get(), iriMap);
    }
}
