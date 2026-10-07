package org.industrial.ontology.kernel.form;

import com.google.common.collect.ImmutableList;
import org.industrial.ontology.domain.form.FormId;
import org.industrial.ontology.domain.core.ProjectId;
import javax.annotation.Nonnull;
import org.industrial.ontology.kernel.api.port.EntityFormRepository;
import org.industrial.ontology.kernel.api.port.EntityFormSelectorRepository;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link FormsCopier}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.form.FormsCopierFactory} (generated in the legacy build).
 */
public final class FormsCopierFactory {

    private final Supplier<EntityFormRepository> entityFormRepository;

    private final Supplier<EntityFormSelectorRepository> entityFormSelectorRepository;

    public FormsCopierFactory(Supplier<EntityFormRepository> entityFormRepository,
            Supplier<EntityFormSelectorRepository> entityFormSelectorRepository) {
        this.entityFormRepository = java.util.Objects.requireNonNull(entityFormRepository);
        this.entityFormSelectorRepository = java.util.Objects.requireNonNull(entityFormSelectorRepository);
    }

    public FormsCopier create(@Nonnull ProjectId fromProjectId, @Nonnull ProjectId toProjectId, @Nonnull ImmutableList<FormId> formsToCopy) {
        return new FormsCopier(fromProjectId, toProjectId, formsToCopy, entityFormRepository.get(), entityFormSelectorRepository.get());
    }
}
