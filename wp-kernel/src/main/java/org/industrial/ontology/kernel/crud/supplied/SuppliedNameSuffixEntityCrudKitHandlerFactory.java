package org.industrial.ontology.kernel.crud.supplied;

import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.semanticweb.owlapi.model.OWLDataFactory;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link SuppliedNameSuffixEntityCrudKitHandler}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.supplied.SuppliedNameSuffixEntityCrudKitHandlerFactory} (generated in the legacy build).
 */
public final class SuppliedNameSuffixEntityCrudKitHandlerFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<EntityIriPrefixResolver> entityIriPrefixResolver;

    public SuppliedNameSuffixEntityCrudKitHandlerFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<EntityIriPrefixResolver> entityIriPrefixResolver) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.entityIriPrefixResolver = java.util.Objects.requireNonNull(entityIriPrefixResolver);
    }

    public SuppliedNameSuffixEntityCrudKitHandler create(@Nonnull EntityCrudKitPrefixSettings prefixSettings, @Nonnull SuppliedNameSuffixSettings settings) {
        return new SuppliedNameSuffixEntityCrudKitHandler(prefixSettings, settings, dataFactory.get(), entityIriPrefixResolver.get());
    }
}
