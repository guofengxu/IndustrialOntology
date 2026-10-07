package org.industrial.ontology.kernel.crud.supplied;



import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;
import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.EntityCrudKitPlugin;
import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixKit;
import org.industrial.ontology.domain.crud.supplied.SuppliedNameSuffixSettings;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.supplied.SuppliedNameSuffixEntityCrudKitPlugin}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class SuppliedNameSuffixEntityCrudKitPlugin implements EntityCrudKitPlugin<SuppliedNameSuffixEntityCrudKitHandler, SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> {

    @Nonnull
    private final SuppliedNameSuffixKit kit;

    @Nonnull
    private final SuppliedNameSuffixEntityCrudKitHandlerFactory factory;

    public SuppliedNameSuffixEntityCrudKitPlugin(@Nonnull SuppliedNameSuffixKit kit,
                                                 @Nonnull SuppliedNameSuffixEntityCrudKitHandlerFactory factory) {
        this.kit = checkNotNull(kit);
        this.factory = checkNotNull(factory);
    }

    @Override
    public EntityCrudKit<SuppliedNameSuffixSettings> getEntityCrudKit() {
        return kit;
    }

    @Override
    public EntityCrudKitHandler<SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> getEntityCrudKitHandler() {
        return factory.create(EntityCrudKitPrefixSettings.get(), SuppliedNameSuffixSettings.get());
    }

    @Override
    public EntityCrudKitHandler<SuppliedNameSuffixSettings, ChangeSetEntityCrudSession> getEntityCrudKitHandler(EntityCrudKitSettings<SuppliedNameSuffixSettings> settings) {
        return factory.create(settings.getPrefixSettings(), settings.getSuffixSettings());
    }

    @Override
    public SuppliedNameSuffixSettings getDefaultSettings() {
        return SuppliedNameSuffixSettings.get();
    }
}
