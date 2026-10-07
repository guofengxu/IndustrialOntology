package org.industrial.ontology.kernel.crud.uuid;

import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.semanticweb.owlapi.model.OWLDataFactory;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link UuidEntityCrudKitHandler}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidEntityCrudKitHandlerFactory} (generated in the legacy build).
 */
public final class UuidEntityCrudKitHandlerFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<EntitiesInProjectSignatureByIriIndex> entitiesInSignature;

    private final Supplier<EntityIriPrefixResolver> entityIriPrefixResolver;

    public UuidEntityCrudKitHandlerFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<EntitiesInProjectSignatureByIriIndex> entitiesInSignature,
            Supplier<EntityIriPrefixResolver> entityIriPrefixResolver) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.entitiesInSignature = java.util.Objects.requireNonNull(entitiesInSignature);
        this.entityIriPrefixResolver = java.util.Objects.requireNonNull(entityIriPrefixResolver);
    }

    public UuidEntityCrudKitHandler create(@Nonnull EntityCrudKitPrefixSettings prefixSettings, @Nonnull UuidSuffixSettings uuidSuffixKitSettings) {
        return new UuidEntityCrudKitHandler(prefixSettings, uuidSuffixKitSettings, dataFactory.get(), entitiesInSignature.get(), entityIriPrefixResolver.get());
    }
}
