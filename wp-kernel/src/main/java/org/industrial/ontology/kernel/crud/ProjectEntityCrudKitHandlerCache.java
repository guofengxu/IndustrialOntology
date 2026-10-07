package org.industrial.ontology.kernel.crud;



import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettings;
import org.industrial.ontology.kernel.api.port.ProjectEntityCrudKitSettingsRepository;
import org.industrial.ontology.domain.crud.EntityCrudKitPrefixSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSettings;
import org.industrial.ontology.domain.crud.EntityCrudKitSuffixSettings;
import org.industrial.ontology.domain.crud.uuid.UuidSuffixSettings;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;

import java.util.Optional;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.ProjectEntityCrudKitHandlerCache}.
 * <p>
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/08/2013
 */
public class ProjectEntityCrudKitHandlerCache {

    private final ProjectId projectId;

    private final ProjectEntityCrudKitSettingsRepository repository;

    private final EntityCrudKitRegistry registry;

    private EntityCrudKitHandler<?, ?> cachedHandler;

    public ProjectEntityCrudKitHandlerCache(@Nonnull ProjectEntityCrudKitSettingsRepository repository,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull EntityCrudKitRegistry registry) {
        this.repository = checkNotNull(repository);
        this.projectId = checkNotNull(projectId);
        this.registry = checkNotNull(registry);
    }

    /**
     * Gets the current {@link EntityCrudKitHandler}.
     * @return The current {@link EntityCrudKitHandler}.  Not {@code null}.
     */
    public synchronized EntityCrudKitHandler<?, ?> getHandler() {
        EntityCrudKitSettings<?> settings = getCurrentSettings();
        if (isCachedHandlerStale(settings)) {
            cachedHandler = registry.getHandler(settings);
        }
        return cachedHandler;
    }

    private EntityCrudKitSettings<?> getCurrentSettings() {
        Optional<ProjectEntityCrudKitSettings> settings = repository.findOne(projectId);
        ProjectEntityCrudKitSettings projectSettings;
        if (!settings.isPresent()) {
            projectSettings = ProjectEntityCrudKitSettings.get(projectId, getDefaultSettings());
            repository.save(projectSettings);
        }
        else {
            projectSettings = settings.get();
        }
        return projectSettings.getSettings();
    }

    private boolean isCachedHandlerStale(EntityCrudKitSettings<?> settings) {
        return cachedHandler == null || !settings.equals(cachedHandler.getSettings());
    }

    /**
     * Creates default settings, with default prefix and suffix settings.
     *
     * @return The default settings.  Not {@code null}.
     */
    private static EntityCrudKitSettings<?> getDefaultSettings() {
        return EntityCrudKitSettings.get(getDefaultPrefixSettings(), getDefaultSuffixSettings());
    }

    private static EntityCrudKitPrefixSettings getDefaultPrefixSettings() {
        return EntityCrudKitPrefixSettings.get();
    }

    private static EntityCrudKitSuffixSettings getDefaultSuffixSettings() {
        return UuidSuffixSettings.get();
    }
}
