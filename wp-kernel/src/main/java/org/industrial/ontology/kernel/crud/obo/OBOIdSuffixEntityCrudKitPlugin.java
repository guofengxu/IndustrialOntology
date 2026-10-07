package org.industrial.ontology.kernel.crud.obo;



import org.industrial.ontology.kernel.crud.EntityCrudKitHandler;
import org.industrial.ontology.kernel.crud.EntityCrudKitPlugin;
import org.industrial.ontology.domain.crud.EntityCrudKit;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.oboid.OBOIdSuffixKit;
import org.industrial.ontology.domain.crud.oboid.OboIdSuffixSettings;

import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.obo.OBOIdSuffixEntityCrudKitPlugin}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 8/19/13
 */
public class OBOIdSuffixEntityCrudKitPlugin implements EntityCrudKitPlugin<OBOIdSuffixEntityCrudKitHandler, OboIdSuffixSettings, OBOIdSession> {

    @Nonnull
    private final OBOIdSuffixKit kit;

    @Nonnull
    private final OBOIdSuffixEntityCrudKitHandlerFactory factory;

    public OBOIdSuffixEntityCrudKitPlugin(@Nonnull OBOIdSuffixKit kit,
                                          @Nonnull OBOIdSuffixEntityCrudKitHandlerFactory factory) {
        this.kit = checkNotNull(kit);
        this.factory = checkNotNull(factory);
    }

    @Override
    public EntityCrudKit<OboIdSuffixSettings> getEntityCrudKit() {
        return kit;
    }

    @Override
    public EntityCrudKitHandler<OboIdSuffixSettings, OBOIdSession> getEntityCrudKitHandler() {
        return factory.create(EntityCrudKitPrefixSettings.get(), OboIdSuffixSettings.get());
    }

    @Override
    public EntityCrudKitHandler<OboIdSuffixSettings, OBOIdSession> getEntityCrudKitHandler(EntityCrudKitSettings<OboIdSuffixSettings> settings) {
        return factory.create(settings.getPrefixSettings(), settings.getSuffixSettings());
    }

    @Override
    public OboIdSuffixSettings getDefaultSettings() {
        return OboIdSuffixSettings.get();
    }
}
