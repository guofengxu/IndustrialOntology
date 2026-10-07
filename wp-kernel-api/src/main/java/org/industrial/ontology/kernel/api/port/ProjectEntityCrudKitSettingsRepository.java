package org.industrial.ontology.kernel.api.port;

import org.industrial.ontology.domain.core.ProjectId;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.persistence.ProjectEntityCrudKitSettingsRepository}.
 * <p>
 * Kernel port: the legacy class read from MongoDB inside the kernel. Here the kernel only sees the methods it
 * calls; wp-app provides the Mongo-backed implementation (docs/01 §1, §5.3).
 */
public interface ProjectEntityCrudKitSettingsRepository {

    Optional<ProjectEntityCrudKitSettings> findOne(@Nonnull ProjectId projectId);

    void save(@Nonnull ProjectEntityCrudKitSettings settings);
}
