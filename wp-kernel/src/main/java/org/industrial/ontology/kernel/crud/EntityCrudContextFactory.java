package org.industrial.ontology.kernel.crud;

import org.industrial.ontology.kernel.api.port.ProjectDetailsRepository;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLOntologyID;
import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link EntityCrudContext}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.crud.EntityCrudContextFactory} (generated in the legacy build).
 */
public final class EntityCrudContextFactory {

    private final Supplier<ProjectId> projectId;

    private final Supplier<ProjectDetailsRepository> projectDetailsRepository;

    public EntityCrudContextFactory(Supplier<ProjectId> projectId,
            Supplier<ProjectDetailsRepository> projectDetailsRepository) {
        this.projectId = java.util.Objects.requireNonNull(projectId);
        this.projectDetailsRepository = java.util.Objects.requireNonNull(projectDetailsRepository);
    }

    public EntityCrudContext create(@Nonnull UserId userId, @Nonnull PrefixedNameExpander prefixedNameExpander, @Nonnull OWLOntologyID targetOntologyId) {
        return new EntityCrudContext(projectId.get(), userId, prefixedNameExpander, projectDetailsRepository.get(), targetOntologyId);
    }
}
