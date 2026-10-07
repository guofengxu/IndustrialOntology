package org.industrial.ontology.kernel.io.upload;



import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.industrial.ontology.kernel.api.revision.Revision;
import org.industrial.ontology.kernel.revision.RevisionStoreFactory;
import org.industrial.ontology.kernel.util.MemoryMonitor;
import org.industrial.ontology.domain.upload.DocumentId;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.core.UserId;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import org.industrial.ontology.kernel.project.Ontology;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.AddImportChange;
import org.industrial.ontology.kernel.api.change.AddOntologyAnnotationChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;


/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.ProjectImporter}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 03/06/15
 */
public class ProjectImporter {

    private static final Logger logger = LoggerFactory.getLogger(ProjectImporter.class);

    @Nonnull
    private final UploadedOntologiesProcessor uploadedOntologiesProcessor;

    @Nonnull
    private final DocumentResolver documentResolver;

    @Nonnull
    private final ProjectId projectId;

    @Nonnull
    private final RevisionStoreFactory revisionStoreFactory;

    public ProjectImporter(ProjectId projectId,
                           @Nonnull UploadedOntologiesProcessor uploadedOntologiesProcessor,
                           @Nonnull DocumentResolver documentResolver,
                           @Nonnull RevisionStoreFactory revisionStoreFactory) {
        this.projectId = checkNotNull(projectId);
        this.uploadedOntologiesProcessor = checkNotNull(uploadedOntologiesProcessor);
        this.documentResolver = checkNotNull(documentResolver);
        this.revisionStoreFactory = checkNotNull(revisionStoreFactory);
    }


    public void createProjectFromSources(DocumentId sourcesId,
                                         UserId owner) throws IOException, OWLOntologyCreationException {
        logger.info("{} Creating project from sources", projectId);
        var stopwatch = Stopwatch.createStarted();
        var uploadedOntologies = uploadedOntologiesProcessor.getUploadedOntologies(sourcesId);
        logger.info("{} Loaded sources in {} ms", projectId, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        var memoryMonitor = new MemoryMonitor(logger);
        memoryMonitor.logMemoryUsage();
        logger.info("{} Writing change log", projectId);
        generateInitialChanges(owner, uploadedOntologies);
        deleteSourceFile(sourcesId);
        logger.info("{} Project creation from sources complete in {} ms", projectId, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        memoryMonitor.logMemoryUsage();

    }

    private void generateInitialChanges(UserId owner, Collection<Ontology> uploadedOntologies) {
        ImmutableList<OntologyChange> changeRecords = getInitialChangeRecords(uploadedOntologies);
        logger.info("{} Writing initial revision containing {} change records", projectId, changeRecords.size());
        Stopwatch stopwatch = Stopwatch.createStarted();
        var revisionStore = revisionStoreFactory.createRevisionStore(projectId);
        revisionStore.append(
                new Revision(
                        owner,
                        RevisionNumber.getRevisionNumber(1),
                        changeRecords,
                        System.currentTimeMillis(),
                        "Initial import"));
        logger.info("{} Initial revision written in {} ms", projectId, stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private ImmutableList<OntologyChange> getInitialChangeRecords(Collection<Ontology> ontologies) {
        ImmutableList.Builder<OntologyChange> changeRecordList = ImmutableList.builder();
        for (var ont : ontologies) {
            var axioms = ont.getAxioms();
            logger.info("{} Processing ontology source ({} axioms)", projectId, axioms.size());
            for (var axiom : ont.getAxioms()) {
                changeRecordList.add(AddAxiomChange.of(ont.getOntologyId(), axiom));
            }
            for (var annotation : ont.getAnnotations()) {
                changeRecordList.add(AddOntologyAnnotationChange.of(ont.getOntologyId(), annotation));
            }
            for (var importsDeclaration : ont.getImportsDeclarations()) {
                changeRecordList.add(AddImportChange.of(ont.getOntologyId(), importsDeclaration));
            }
        }
        return changeRecordList.build();
    }

    private void deleteSourceFile(DocumentId sourceFileId) {
        var sourceFilePath = documentResolver.resolve(sourceFileId);
        try {
            Files.deleteIfExists(sourceFilePath);
        } catch(IOException e) {
            logger.info("Could not delete uploaded file: {} Cause: {}", sourceFilePath, e.getMessage());
        }
    }
}
