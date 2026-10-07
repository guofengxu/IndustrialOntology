package org.industrial.ontology.kernel.api.project;



import org.industrial.ontology.kernel.api.util.DisposableObjectManager;
import org.industrial.ontology.domain.core.HasDispose;
import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ProjectDisposablesManager}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Nov 2018
 */
public class ProjectDisposablesManager {

    @Nonnull
    private final DisposableObjectManager disposableObjectManager;

    public ProjectDisposablesManager(@Nonnull DisposableObjectManager disposableObjectManager) {
        this.disposableObjectManager = checkNotNull(disposableObjectManager);
    }

    public void register(@Nonnull HasDispose dispose) {
        disposableObjectManager.register(dispose);
    }

    public void dispose() {
        disposableObjectManager.dispose();
    }
}
