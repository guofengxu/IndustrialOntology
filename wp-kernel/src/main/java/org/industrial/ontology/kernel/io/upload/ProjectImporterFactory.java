package org.industrial.ontology.kernel.io.upload;

import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.industrial.ontology.domain.core.ProjectId;
import java.util.function.Supplier;

/**
 * Hand-written replacement for the {@code @AutoFactory}-generated factory of {@link ProjectImporter}.
 * <p>
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ProjectImporterFactory} (generated in the legacy build).
 */
public final class ProjectImporterFactory {

    private final Supplier<UploadedOntologiesProcessor> uploadedOntologiesProcessor;

    private final Supplier<DocumentResolver> documentResolver;

    private final Supplier<RevisionStoreFactory> revisionStoreFactory;

    public ProjectImporterFactory(Supplier<UploadedOntologiesProcessor> uploadedOntologiesProcessor,
            Supplier<DocumentResolver> documentResolver,
            Supplier<RevisionStoreFactory> revisionStoreFactory) {
        this.uploadedOntologiesProcessor = java.util.Objects.requireNonNull(uploadedOntologiesProcessor);
        this.documentResolver = java.util.Objects.requireNonNull(documentResolver);
        this.revisionStoreFactory = java.util.Objects.requireNonNull(revisionStoreFactory);
    }

    public ProjectImporter create(ProjectId projectId) {
        return new ProjectImporter(projectId, uploadedOntologiesProcessor.get(), documentResolver.get(), revisionStoreFactory.get());
    }
}
