package org.industrial.ontology.kernel.io.upload;



import com.google.common.collect.Lists;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentParserFactory;
import org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParserFactory;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.krss2.parser.KRSS2OWLParserFactory;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxOntologyParserFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.oboformat.OBOFormatOWLAPIParserFactory;
import org.semanticweb.owlapi.owlxml.parser.OWLXMLParserFactory;
import org.semanticweb.owlapi.rdf.rdfxml.parser.RDFXMLParserFactory;
import org.semanticweb.owlapi.rdf.turtle.parser.TurtleOntologyParserFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Ported from {@code edu.stanford.bmir.protege.web.server.project.RawProjectSourcesImporter}.
 * <p>
 * Unlike the legacy importer, the imports of the uploaded documents are resolved only to other documents of the
 * upload. While the sources load, the manager's IRI mappers are replaced by one that maps every other import to
 * {@link #IMPORT_NOT_LOADED}, and a factory is added that fails to load that document with an I/O error. OWL API
 * reports the import as missing, which the loader configuration ignores (the legacy importer used
 * {@link org.semanticweb.owlapi.model.MissingImportHandlingStrategy#SILENT} for imports it could not fetch). The
 * legacy importer let OWL API fetch every import IRI, so an uploaded document could make the server read local files
 * ({@code file:} IRIs) or request other hosts, and merge whatever it got into the new project.
 * <p>
 * For the same reason, the uploads are read only with the parsers in {@link #UPLOAD_PARSERS}. The manager's mappers,
 * parsers and factories are restored afterwards.
 * <p>
 * @author Matthew Horridge,
 *         Stanford University,
 *         Bio-Medical Informatics Research Group
 *         Date: 19/02/2014
 */
public class RawProjectSourcesImporter {

    /**
     * Where imports that the upload does not contain are "found". Only {@link ImportNotLoadedFactory} accepts it.
     */
    static final IRI IMPORT_NOT_LOADED = IRI.create("urn:industrial-ontology:import-not-loaded");

    /**
     * The parsers that read uploads: OWL API's own (RDF/XML, OWL/XML, functional, Manchester, Turtle, which also reads
     * N-Triples, OBO, KRSS2) and BinaryOWL. Left out are the Rio (Sesame) parsers that OWL API also registers: the
     * JSON-LD parser fetches the contexts that a document names, so a {@code file:} context reads a local file; the
     * RDF/JSON parser throws an unchecked exception for other JSON, so the upload failed as a server error; the XML
     * and HTML ones (RDF/XML, TriX, RDFa) are not configured against external entities, whereas OWL API's RDF/XML
     * parser is; and most of them leave the document open when they fail. N-Quads, TriG, JSON-LD, RDF/JSON, TriX and
     * RDFa documents are therefore not accepted.
     */
    private static final Set<Class<?>> UPLOAD_PARSERS = Set.of(BinaryOWLOntologyDocumentParserFactory.class,
                                                               RDFXMLParserFactory.class,
                                                               OWLXMLParserFactory.class,
                                                               OWLFunctionalSyntaxOWLParserFactory.class,
                                                               ManchesterOWLSyntaxOntologyParserFactory.class,
                                                               TurtleOntologyParserFactory.class,
                                                               OBOFormatOWLAPIParserFactory.class,
                                                               KRSS2OWLParserFactory.class);

    private OWLOntologyManager manager;

    private OWLOntologyLoaderConfiguration loaderConfig;

    public RawProjectSourcesImporter(OWLOntologyManager manager, OWLOntologyLoaderConfiguration loaderConfig) {
        this.manager = manager;
        this.loaderConfig = loaderConfig;
    }

    public OWLOntology importRawProjectSources(RawProjectSources projectSources) throws OWLOntologyCreationException {
        var mappers = manager.getIRIMappers();
        List<OWLOntologyIRIMapper> previousMappers = Lists.newArrayList(mappers);
        var parsers = manager.getOntologyParsers();
        List<OWLParserFactory> previousParsers = Lists.newArrayList(parsers);
        var importNotLoadedFactory = new ImportNotLoadedFactory();
        try {
            mappers.set(new UploadedDocumentsOnlyIRIMapper(projectSources.getOntologyIRIMapper()));
            parsers.set(previousParsers.stream()
                                       .filter(parser -> UPLOAD_PARSERS.contains(parser.getClass()))
                                       .collect(toList()));
            manager.getOntologyFactories().add(importNotLoadedFactory);
            OWLOntology ontology = null;
            for (OWLOntologyDocumentSource documentSource : projectSources.getDocumentSources()) {
                ontology = manager.loadOntologyFromOntologyDocument(documentSource, loaderConfig);
            }
            return ontology;
        } finally {
            manager.getOntologyFactories().remove(importNotLoadedFactory);
            parsers.set(previousParsers);
            mappers.set(previousMappers);
        }
    }

    /**
     * Maps an ontology IRI to its document in the upload, and every other ontology IRI to {@link #IMPORT_NOT_LOADED}.
     */
    private static final class UploadedDocumentsOnlyIRIMapper implements OWLOntologyIRIMapper {

        private static final long serialVersionUID = 1L;

        private final OWLOntologyIRIMapper uploadedDocuments;

        private UploadedDocumentsOnlyIRIMapper(@Nonnull OWLOntologyIRIMapper uploadedDocuments) {
            this.uploadedDocuments = checkNotNull(uploadedDocuments);
        }

        @Nullable
        @Override
        public IRI getDocumentIRI(@Nonnull IRI ontologyIRI) {
            var documentIri = uploadedDocuments.getDocumentIRI(ontologyIRI);
            return documentIri != null ? documentIri : IMPORT_NOT_LOADED;
        }
    }

    /**
     * Fails to load {@link #IMPORT_NOT_LOADED} with an I/O error, the checked failure that OWL API hands to the
     * missing import handling. No other factory accepts that IRI, and without this one OWL API would throw the
     * unchecked {@code OWLOntologyFactoryNotFoundException}, which aborts the whole upload.
     */
    private static final class ImportNotLoadedFactory implements OWLOntologyFactory {

        private static final long serialVersionUID = 1L;

        @Override
        public boolean canCreateFromDocumentIRI(@Nonnull IRI documentIRI) {
            return false;
        }

        @Override
        public boolean canLoad(@Nonnull OWLOntologyDocumentSource documentSource) {
            return IMPORT_NOT_LOADED.equals(documentSource.getDocumentIRI());
        }

        @Nonnull
        @Override
        public OWLOntology createOWLOntology(@Nonnull OWLOntologyManager manager,
                                             @Nonnull OWLOntologyID ontologyID,
                                             @Nonnull IRI documentIRI,
                                             @Nonnull OWLOntologyCreationHandler handler)
                throws OWLOntologyCreationException {
            throw new OWLOntologyCreationException("Cannot create an ontology at " + documentIRI);
        }

        @Nonnull
        @Override
        public OWLOntology loadOWLOntology(@Nonnull OWLOntologyManager manager,
                                           @Nonnull OWLOntologyDocumentSource documentSource,
                                           @Nonnull OWLOntologyCreationHandler handler,
                                           @Nonnull OWLOntologyLoaderConfiguration configuration)
                throws OWLOntologyCreationException {
            throw new OWLOntologyCreationIOException(new IOException("The import is not part of the upload"));
        }
    }
}
