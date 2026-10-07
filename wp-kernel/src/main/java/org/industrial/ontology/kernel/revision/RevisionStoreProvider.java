package org.industrial.ontology.kernel.revision;

import org.industrial.ontology.kernel.api.project.ProjectDisposablesManager;
import javax.annotation.Nonnull;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionStoreProvider}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 02/06/15
 */
public class RevisionStoreProvider {

    @Nonnull
    private final BinaryOwlRevisionStore revisionStore;

    @Nonnull
    private final ProjectDisposablesManager disposablesManager;

    private boolean loaded = false;

    public RevisionStoreProvider(@Nonnull BinaryOwlRevisionStore revisionStore, @Nonnull ProjectDisposablesManager disposablesManager) {
        this.revisionStore = checkNotNull(revisionStore);
        this.disposablesManager = checkNotNull(disposablesManager);
    }

    public synchronized RevisionStore get() {
        if (!loaded) {
            revisionStore.load();
            loaded = true;
            disposablesManager.register(revisionStore);
        }
        return revisionStore;
    }
}
