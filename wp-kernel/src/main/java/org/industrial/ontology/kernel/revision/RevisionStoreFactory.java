package org.industrial.ontology.kernel.revision;



import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslator;
import org.industrial.ontology.kernel.project.ChangeHistoryFileFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.semanticweb.owlapi.model.OWLDataFactory;
import javax.annotation.Nonnull;

import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.revision.RevisionStoreFactory}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-29
 */
public class RevisionStoreFactory {

    @Nonnull
    private final ChangeHistoryFileFactory changeHistoryFileFactory;

    @Nonnull
    private final OWLDataFactory dataFactory;

    @Nonnull
    private final OntologyChangeRecordTranslator changeRecordTranslator;

    @Nonnull
    private final Executor serializationExecutor;

    /**
     * @param serializationExecutor executor shared by all projects for writing revisions to disk; the stores created
     *                              here never shut it down
     */
    public RevisionStoreFactory(@Nonnull ChangeHistoryFileFactory changeHistoryFileFactory,
                                @Nonnull OWLDataFactory dataFactory,
                                @Nonnull OntologyChangeRecordTranslator changeRecordTranslator,
                                @Nonnull Executor serializationExecutor) {
        this.changeHistoryFileFactory = checkNotNull(changeHistoryFileFactory);
        this.dataFactory = checkNotNull(dataFactory);
        this.changeRecordTranslator = checkNotNull(changeRecordTranslator);
        this.serializationExecutor = checkNotNull(serializationExecutor);
    }

    @Nonnull
    public RevisionStore createRevisionStore(@Nonnull ProjectId projectId) {
        checkNotNull(projectId);
        var revisionStore = new BinaryOwlRevisionStore(projectId,
                                     changeHistoryFileFactory,
                                     dataFactory,
                                     changeRecordTranslator,
                                     serializationExecutor);
        revisionStore.load();
        return revisionStore;
    }

}
