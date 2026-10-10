package org.industrial.ontology.kernel.io.upload;

import org.industrial.ontology.kernel.owlapi.WebProtegeOWLManager;
import org.industrial.ontology.kernel.project.Ontology;
import org.industrial.ontology.domain.upload.DocumentId;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collection;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import java.util.function.Supplier;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.upload.UploadedOntologiesProcessor}.
 * <p>
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2019-08-20
 *
 * Processes an uploaded file as an ontology document or zip of ontology documents.
 */
public class UploadedOntologiesProcessor {

    private static final Logger logger = LoggerFactory.getLogger(UploadedOntologiesProcessor.class);

    @Nonnull
    private final DocumentResolver documentResolver;

    @Nonnull
    private final Supplier<UploadedProjectSourcesExtractor> uploadedProjectSourcesExtractorProvider;

    public UploadedOntologiesProcessor(@Nonnull DocumentResolver documentResolver, @Nonnull Supplier<UploadedProjectSourcesExtractor> uploadedProjectSourcesExtractorProvider) {
        this.documentResolver = checkNotNull(documentResolver);
        this.uploadedProjectSourcesExtractorProvider = uploadedProjectSourcesExtractorProvider;
    }

    @Nonnull
    public Collection<Ontology> getUploadedOntologies(@Nonnull DocumentId documentId) throws OWLOntologyCreationException, IOException {
        return loadOntologies(documentId);
    }

    /**
     * Unlike the legacy version, the files extracted from a zip archive are deleted once the ontologies are loaded
     * (the legacy processor never called {@link RawProjectSources#cleanUpTemporaryFiles()}).
     */
    private Collection<Ontology> loadOntologies(@Nonnull DocumentId documentId) throws IOException, OWLOntologyCreationException {
        var manager = WebProtegeOWLManager.createOWLOntologyManager();
        var uploadedFile = documentResolver.resolve(documentId).toFile();
        var uploadedProjectSourcesExtractor = uploadedProjectSourcesExtractorProvider.get();
        var rawProjectSources = uploadedProjectSourcesExtractor.extractProjectSources(uploadedFile);
        try {
            var loaderConfig = new OWLOntologyLoaderConfiguration().// See https://github.com/protegeproject/webprotege/issues/700
            setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
            var rawProjectSourcesImporter = new RawProjectSourcesImporter(manager, loaderConfig);
            rawProjectSourcesImporter.importRawProjectSources(rawProjectSources);
            return manager.getOntologies().stream().map(this::toOntology).collect(toList());
        } finally {
            cleanUp(documentId, rawProjectSources);
        }
    }

    private static void cleanUp(DocumentId documentId, RawProjectSources rawProjectSources) {
        try {
            rawProjectSources.cleanUpTemporaryFiles();
        } catch (IOException e) {
            logger.warn("Could not delete the files extracted from upload {}: {}", documentId.getDocumentId(),
                        e.getMessage());
        }
    }

    private Ontology toOntology(OWLOntology ont) {
        return Ontology.get(ont.getOntologyID(), ont.getImportsDeclarations(), ont.getAnnotations(), ont.getAxioms());
    }
}
