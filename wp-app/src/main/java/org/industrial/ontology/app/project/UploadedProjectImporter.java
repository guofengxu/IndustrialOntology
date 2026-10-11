package org.industrial.ontology.app.project;

import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.kernel.change.OntologyChangeRecordTranslatorImpl;
import org.industrial.ontology.kernel.io.upload.ProjectImporter;
import org.industrial.ontology.kernel.io.upload.RootOntologyDocumentMatcher;
import org.industrial.ontology.kernel.io.upload.SingleDocumentProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.UploadedOntologiesProcessor;
import org.industrial.ontology.kernel.io.upload.UploadedProjectSourcesExtractor;
import org.industrial.ontology.kernel.io.upload.ZipArchiveProjectSourcesExtractor;
import org.industrial.ontology.kernel.project.DataDirectoryLayout;
import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.industrial.ontology.kernel.util.TempFileFactoryImpl;
import org.industrial.ontology.kernel.util.ZipInputStreamChecker;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Writes the first revision of a new project from an uploaded document or zip archive, as the legacy
 * {@code ProjectCache.getProject(NewProjectSettings)} did with its {@code ProjectImporter} (docs/01 §3.3). The
 * document comes from the uploads directory ({@link UploadService}) and is deleted once it has been imported.
 */
public class UploadedProjectImporter {

    /**
     * The ids that {@link UploadService} gives (UUIDs) and those of the legacy servlet ({@code upload-<digits>}):
     * a single file name in the uploads directory, never a path.
     */
    private static final Pattern DOCUMENT_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,127}");

    private final DataDirectoryLayout dataDirectoryLayout;

    private final KernelExecutors kernelExecutors;

    public UploadedProjectImporter(@Nonnull DataDirectoryLayout dataDirectoryLayout,
                                   @Nonnull KernelExecutors kernelExecutors) {
        this.dataDirectoryLayout = checkNotNull(dataDirectoryLayout);
        this.kernelExecutors = checkNotNull(kernelExecutors);
    }

    /**
     * Whether the uploads directory has the document; an id that is not a plain file name is never there.
     */
    public boolean exists(@Nonnull DocumentId documentId) {
        return DOCUMENT_ID.matcher(documentId.getDocumentId()).matches()
                && Files.isRegularFile(dataDirectoryLayout.getDocumentResolver().resolve(documentId));
    }

    /**
     * Parses the document and writes its axioms, ontology annotations and imports as revision 1 of the project, by
     * {@code owner}, then deletes the document.
     *
     * @throws OWLOntologyCreationException if the document is not an ontology that can be parsed
     * @throws IOException                  if the document cannot be read or the revision cannot be written
     */
    public void importProject(@Nonnull ProjectId projectId,
                              @Nonnull DocumentId documentId,
                              @Nonnull UserId owner) throws IOException, OWLOntologyCreationException {
        var documentResolver = dataDirectoryLayout.getDocumentResolver();
        var uploadedOntologiesProcessor = new UploadedOntologiesProcessor(
                documentResolver,
                () -> new UploadedProjectSourcesExtractor(new ZipInputStreamChecker(),
                                                          new ZipArchiveProjectSourcesExtractor(
                                                                  new TempFileFactoryImpl(),
                                                                  new RootOntologyDocumentMatcher()),
                                                          new SingleDocumentProjectSourcesExtractor()));
        var revisionStoreFactory = new RevisionStoreFactory(dataDirectoryLayout.getChangeHistoryFileFactory(),
                                                            new OWLDataFactoryImpl(),
                                                            new OntologyChangeRecordTranslatorImpl(),
                                                            kernelExecutors.threadPools().revisionWrites());
        new ProjectImporter(projectId, uploadedOntologiesProcessor, documentResolver, revisionStoreFactory)
                .createProjectFromSources(documentId, owner);
    }
}
