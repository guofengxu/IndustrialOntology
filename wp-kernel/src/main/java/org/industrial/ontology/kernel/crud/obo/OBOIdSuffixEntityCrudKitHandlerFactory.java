package org.industrial.ontology.kernel.crud.obo;

import org.industrial.ontology.kernel.crud.EntityIriPrefixResolver;
import org.industrial.ontology.kernel.api.index.EntitiesInProjectSignatureByIriIndex;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.oboid.OboIdSuffixSettings;
import javax.annotation.Nonnull;
import org.semanticweb.owlapi.model.OWLDataFactory;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link OBOIdSuffixEntityCrudKitHandler}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OBOIdSuffixEntityCrudKitHandlerFactory} (generated in the legacy build).
 */
public final class OBOIdSuffixEntityCrudKitHandlerFactory {

    private final Supplier<OWLDataFactory> dataFactory;

    private final Supplier<EntitiesInProjectSignatureByIriIndex> projectSignatureIndex;

    private final Supplier<EntityIriPrefixResolver> entityIriPrefixResolver;

    public OBOIdSuffixEntityCrudKitHandlerFactory(Supplier<OWLDataFactory> dataFactory,
            Supplier<EntitiesInProjectSignatureByIriIndex> projectSignatureIndex,
            Supplier<EntityIriPrefixResolver> entityIriPrefixResolver) {
        this.dataFactory = java.util.Objects.requireNonNull(dataFactory);
        this.projectSignatureIndex = java.util.Objects.requireNonNull(projectSignatureIndex);
        this.entityIriPrefixResolver = java.util.Objects.requireNonNull(entityIriPrefixResolver);
    }

    public OBOIdSuffixEntityCrudKitHandler create(@Nonnull EntityCrudKitPrefixSettings prefixSettings, @Nonnull OboIdSuffixSettings suffixSettings) {
        return new OBOIdSuffixEntityCrudKitHandler(prefixSettings, suffixSettings, dataFactory.get(), projectSignatureIndex.get(), entityIriPrefixResolver.get());
    }
}
