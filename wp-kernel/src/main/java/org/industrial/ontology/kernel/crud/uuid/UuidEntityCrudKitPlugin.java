package org.industrial.ontology.kernel.crud.uuid;



import org.industrial.ontology.kernel.crud.ChangeSetEntityCrudSession;
import org.industrial.ontology.kernel.crud.EntityCrudKitPlugin;
import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixKit;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.uuid.UuidEntityCrudKitPlugin}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class UuidEntityCrudKitPlugin implements EntityCrudKitPlugin<UuidEntityCrudKitHandler, UuidSuffixSettings, ChangeSetEntityCrudSession> {

    @Nonnull
    private UuidSuffixKit kit;

    @Nonnull
    private final UuidEntityCrudKitHandlerFactory factory;

    public UuidEntityCrudKitPlugin(@Nonnull UuidSuffixKit kit,
                                   @Nonnull UuidEntityCrudKitHandlerFactory factory) {
        this.kit = checkNotNull(kit);
        this.factory = checkNotNull(factory);
    }

    @Override
    public EntityCrudKit<UuidSuffixSettings> getEntityCrudKit() {
        return kit;
    }

    @Override
    public UuidEntityCrudKitHandler getEntityCrudKitHandler() {
        return factory.create(EntityCrudKitPrefixSettings.get(), UuidSuffixSettings.get());
    }

    @Override
    public UuidSuffixSettings getDefaultSettings() {
        return UuidSuffixSettings.get();
    }

    @Override
    public UuidEntityCrudKitHandler getEntityCrudKitHandler(EntityCrudKitSettings<UuidSuffixSettings> settings) {
        return factory.create(settings.getPrefixSettings(), settings.getSuffixSettings());
    }
}
