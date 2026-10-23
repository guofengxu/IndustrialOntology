package org.industrial.ontology.app.frame;

import org.industrial.ontology.app.access.PermissionDeniedException;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.project.ChangeOutcome;
import org.industrial.ontology.app.project.GuardedChange;
import org.industrial.ontology.app.project.ProjectContexts;
import org.industrial.ontology.app.project.ProjectNotFoundException;
import org.industrial.ontology.domain.core.DataFactory;
import org.industrial.ontology.domain.core.ProjectId;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.kernel.api.change.AddAxiomChange;
import org.industrial.ontology.kernel.api.change.OntologyChange;
import org.industrial.ontology.kernel.api.change.RemoveAxiomChange;
import org.industrial.ontology.kernel.change.FixedChangeListGenerator;
import org.industrial.ontology.kernel.io.upload.RawProjectSources;
import org.industrial.ontology.kernel.io.upload.RawProjectSourcesImporter;
import org.industrial.ontology.kernel.owlapi.WebProtegeOWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.domain.core.BuiltInAction.EDIT_ONTOLOGY;

/**
 * Adds and deletes axioms given as an ontology document (docs/01 §5.1, stage S7): the legacy {@code AddAxioms} and
 * {@code DeleteAxioms} handlers, which the REST API offered for scripts. The axioms go to, or are deleted from, the
 * root ontology, in one revision by the caller.
 * <p>
 * Both need {@code EditOntology}; the legacy delete handler checked nothing itself and relied on the legacy
 * {@code ChangeManager}'s check. The document is read the way uploaded projects are read
 * ({@link RawProjectSourcesImporter}): only with the parser of its syntax, and without fetching what it imports,
 * whose declarations are ignored, like its ontology annotations.
 */
public class AxiomsService {

    public static final String UNPARSABLE_AXIOMS = "UNPARSABLE_AXIOMS";

    /** The base of relative IRIs in the document, which has no location. */
    private static final IRI DOCUMENT_IRI = IRI.create("urn:industrial-ontology:posted-axioms");

    private final ProjectContexts projects;

    public AxiomsService(@Nonnull ProjectContexts projects) {
        this.projects = checkNotNull(projects);
    }

    /**
     * Adds the document's axioms to the root ontology.
     *
     * @param commitMessage describes the revision; without one it is "Added axioms"
     * @return how many of the axioms were not in the ontology already, and were added
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@value #UNPARSABLE_AXIOMS} (400) if the document does not parse in the syntax
     */
    @Nonnull
    public ChangeOutcome<Integer> addAxioms(@Nonnull UserId caller,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull String document,
                                            @Nonnull Syntax syntax,
                                            @Nullable String commitMessage) {
        return addAxioms(caller, projectId, () -> document, syntax, commitMessage);
    }

    /**
     * As {@link #addAxioms(UserId, ProjectId, String, Syntax, String)}, reading the document only once the project
     * and the caller's permission have been checked.
     */
    @Nonnull
    public ChangeOutcome<Integer> addAxioms(@Nonnull UserId caller,
                                            @Nonnull ProjectId projectId,
                                            @Nonnull Supplier<String> document,
                                            @Nonnull Syntax syntax,
                                            @Nullable String commitMessage) {
        return apply(caller, projectId, document, syntax, commitMessage, AddAxiomChange::of, "Added axioms");
    }

    /**
     * Deletes the document's axioms from the root ontology.
     *
     * @param commitMessage describes the revision; without one it is "Deleted axioms"
     * @return how many of the axioms were in the ontology, and were deleted
     * @throws ProjectNotFoundException  if the project does not exist
     * @throws PermissionDeniedException if the caller may not edit the ontology
     * @throws WpException               {@value #UNPARSABLE_AXIOMS} (400) if the document does not parse in the syntax
     */
    @Nonnull
    public ChangeOutcome<Integer> deleteAxioms(@Nonnull UserId caller,
                                               @Nonnull ProjectId projectId,
                                               @Nonnull String document,
                                               @Nonnull Syntax syntax,
                                               @Nullable String commitMessage) {
        return deleteAxioms(caller, projectId, () -> document, syntax, commitMessage);
    }

    /**
     * As {@link #deleteAxioms(UserId, ProjectId, String, Syntax, String)}, reading the document only once the
     * project and the caller's permission have been checked.
     */
    @Nonnull
    public ChangeOutcome<Integer> deleteAxioms(@Nonnull UserId caller,
                                               @Nonnull ProjectId projectId,
                                               @Nonnull Supplier<String> document,
                                               @Nonnull Syntax syntax,
                                               @Nullable String commitMessage) {
        return apply(caller, projectId, document, syntax, commitMessage, RemoveAxiomChange::of, "Deleted axioms");
    }

    private ChangeOutcome<Integer> apply(UserId caller,
                                         ProjectId projectId,
                                         Supplier<String> document,
                                         Syntax syntax,
                                         @Nullable String commitMessage,
                                         BiFunction<OWLOntologyID, OWLAxiom, OntologyChange> change,
                                         String defaultMessage) {
        var context = projects.open(caller, projectId, EDIT_ONTOLOGY);
        var axioms = parse(document.get(), syntax);
        // Such IRIs stand for entities that ChangeManager is to create; a document names existing ones
        if (axioms.stream().flatMap(axiom -> axiom.getSignature().stream()).anyMatch(DataFactory::isFreshEntity)) {
            throw new WpException(UNPARSABLE_AXIOMS, 400, "The document uses the reserved IRI scheme entity:");
        }
        var ontologyId = context.defaultOntologyIdManager().getDefaultOntologyId();
        List<OntologyChange> changes = axioms.stream().map(axiom -> change.apply(ontologyId, axiom)).toList();
        var generator = GuardedChange.of(new FixedChangeListGenerator<>(changes, 0, defaultMessage))
                                     .withCommitMessage(commitMessage);
        // The effective changes are those that were not already applied: their number is the result
        var outcome = ProjectContexts.apply(context, caller, generator);
        var applied = outcome.revision().map(revision -> revision.getSize()).orElse(0);
        return outcome.map(ignored -> applied);
    }

    /**
     * The axioms of the document, read without its imports.
     *
     * @throws WpException {@value #UNPARSABLE_AXIOMS} (400) if the document does not parse in the syntax
     */
    @Nonnull
    static Set<OWLAxiom> parse(@Nonnull String document, @Nonnull Syntax syntax) {
        var manager = WebProtegeOWLManager.createOWLOntologyManager();
        var source = new StringDocumentSource(document, DOCUMENT_IRI, syntax.format.get(), syntax.mimeType);
        var loaderConfig = new OWLOntologyLoaderConfiguration()
                .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        try {
            var ontology = new RawProjectSourcesImporter(manager, loaderConfig).importRawProjectSources(
                    new SingleDocument(source));
            return ontology.getAxioms();
        } catch (OWLOntologyCreationException | RuntimeException e) {
            throw new WpException(UNPARSABLE_AXIOMS, 400,
                                  "The document is not " + syntax.displayName + ": "
                                          + Optional.ofNullable(e.getMessage()).map(AxiomsService::firstLine)
                                                    .orElse(e.getClass().getSimpleName()));
        }
    }

    private static String firstLine(String message) {
        var end = message.indexOf('\n');
        return (end < 0 ? message : message.substring(0, end)).strip();
    }

    /**
     * The syntaxes of the documents, with their media types (docs/02 §4.3).
     */
    public enum Syntax {

        TURTLE("text/turtle", "Turtle", TurtleDocumentFormat::new),

        RDF_XML("application/rdf+xml", "RDF/XML", RDFXMLDocumentFormat::new),

        FUNCTIONAL("text/owl-functional", "OWL functional syntax", FunctionalSyntaxDocumentFormat::new);

        private final String mimeType;

        private final String displayName;

        private final Supplier<OWLDocumentFormat> format;

        Syntax(String mimeType, String displayName, Supplier<OWLDocumentFormat> format) {
            this.mimeType = mimeType;
            this.displayName = displayName;
            this.format = format;
        }

        @Nonnull
        public String getMimeType() {
            return mimeType;
        }

        /**
         * The syntax of the media type, ignoring parameters such as {@code charset}.
         */
        @Nonnull
        public static Optional<Syntax> forMimeType(@Nonnull String mimeType) {
            var type = mimeType.split(";", 2)[0].strip();
            for (var syntax : values()) {
                if (syntax.mimeType.equalsIgnoreCase(type)) {
                    return Optional.of(syntax);
                }
            }
            return Optional.empty();
        }
    }

    /**
     * The posted document as the importer's sources: it imports nothing that the importer would resolve.
     */
    private record SingleDocument(OWLOntologyDocumentSource source) implements RawProjectSources {

        @Override
        public Collection<OWLOntologyDocumentSource> getDocumentSources() {
            return List.of(source);
        }

        @Override
        public OWLOntologyIRIMapper getOntologyIRIMapper() {
            return ontologyIri -> null;
        }

        @Override
        public void cleanUpTemporaryFiles() {
            // Nothing was written
        }
    }
}
