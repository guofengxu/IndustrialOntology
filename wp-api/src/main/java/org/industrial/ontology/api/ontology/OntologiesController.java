package org.industrial.ontology.api.ontology;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.industrial.ontology.api.ontology.OntologyJson.ChangeOutcomeDto;
import org.industrial.ontology.api.ontology.OntologyJson.PropertyValueDto;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.frame.AxiomsService;
import org.industrial.ontology.app.frame.AxiomsService.Syntax;
import org.industrial.ontology.app.frame.OntologyService;
import org.industrial.ontology.app.frame.OntologyService.OntologyAnnotations;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.frame.PlainPropertyAnnotationValue;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.industrial.ontology.api.ontology.OntologyParams.required;

/**
 * A project's ontologies, their annotations, and axioms given as ontology documents (docs/02 §4.3). An ontology is
 * named by {@code ontologyIri}; without it the root ontology is meant. The services check the permissions.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Ontologies", description = "Ontologies, ontology annotations and axioms")
public class OntologiesController {

    static final String PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE";

    /** The largest axioms document read; the whole document is parsed in memory. */
    static final int MAX_DOCUMENT_BYTES = 10 * 1024 * 1024;

    private static final String[] AXIOM_MEDIA_TYPES = {"text/turtle", "application/rdf+xml", "text/owl-functional"};

    private final OntologyService ontologyService;

    private final AxiomsService axiomsService;

    public OntologiesController(OntologyService ontologyService, AxiomsService axiomsService) {
        this.ontologyService = checkNotNull(ontologyService);
        this.axiomsService = checkNotNull(axiomsService);
    }

    @Operation(summary = "The project's ontologies, the root first (permission ViewProject)")
    @GetMapping("/ontologies")
    public List<OntologyDto> ontologies(@Caller UserId caller, @PathVariable String projectId) {
        return ontologyService.getOntologies(caller, ProjectIds.parse(projectId))
                              .stream()
                              .map(summary -> new OntologyDto(iriOf(summary.ontologyId()),
                                                              versionIriOf(summary.ontologyId()),
                                                              summary.shortForm(),
                                                              summary.root()))
                              .toList();
    }

    @Operation(summary = "The ontology's annotations, plain and rendered (permission ViewProject)")
    @GetMapping("/ontologies/annotations")
    public OntologyAnnotationsDto annotations(@Caller UserId caller,
                                              @PathVariable String projectId,
                                              @RequestParam(required = false) String ontologyIri) {
        return OntologyAnnotationsDto.of(ontologyService.getAnnotations(caller, ProjectIds.parse(projectId),
                                                                        optionalIri(ontologyIri)));
    }

    @Operation(summary = "Replaces the annotations from with to (permissions EditOntology and "
            + "EditOntologyAnnotations)")
    @PutMapping("/ontologies/annotations")
    public ChangeOutcomeDto<OntologyAnnotationsDto> setAnnotations(@Caller UserId caller,
                                                                   @PathVariable String projectId,
                                                                   @RequestParam(required = false)
                                                                   String ontologyIri,
                                                                   @RequestBody AnnotationsUpdateRequest request) {
        var outcome = ontologyService.setAnnotations(caller,
                                                     ProjectIds.parse(projectId),
                                                     optionalIri(ontologyIri),
                                                     required(request.from(), "from"),
                                                     required(request.to(), "to"),
                                                     request.commitMessage());
        return ChangeOutcomeDto.of(outcome, OntologyAnnotationsDto::of);
    }

    @Operation(summary = "Adds the axioms of the document to the root ontology (permission EditOntology)",
               description = "The body is Turtle, RDF/XML or OWL functional syntax, as its Content-Type says, of "
                       + "at most 10 MB; its imports are not fetched.")
    @PostMapping(value = "/axioms", consumes = {"text/turtle", "application/rdf+xml", "text/owl-functional"})
    @ResponseStatus(HttpStatus.CREATED)
    public ChangeOutcomeDto<AddedAxiomsDto> addAxioms(@Caller UserId caller,
                                                      @PathVariable String projectId,
                                                      @RequestParam(required = false) String commitMessage,
                                                      HttpServletRequest request) throws IOException {
        var project = ProjectIds.parse(projectId);
        var syntax = syntax(request);
        requireDeclaredSizeWithinLimit(request);
        var outcome = axiomsService.addAxioms(caller, project, () -> document(request), syntax, commitMessage);
        return ChangeOutcomeDto.of(outcome, AddedAxiomsDto::new);
    }

    @Operation(summary = "Deletes the axioms of the document from the root ontology (permission EditOntology)",
               description = "The body is as for adding axioms.")
    @PostMapping(value = "/axioms/delete",
                 consumes = {"text/turtle", "application/rdf+xml", "text/owl-functional"})
    public ChangeOutcomeDto<DeletedAxiomsDto> deleteAxioms(@Caller UserId caller,
                                                           @PathVariable String projectId,
                                                           @RequestParam(required = false) String commitMessage,
                                                           HttpServletRequest request) throws IOException {
        var project = ProjectIds.parse(projectId);
        var syntax = syntax(request);
        requireDeclaredSizeWithinLimit(request);
        var outcome = axiomsService.deleteAxioms(caller, project, () -> document(request), syntax, commitMessage);
        return ChangeOutcomeDto.of(outcome, DeletedAxiomsDto::new);
    }

    private static Syntax syntax(HttpServletRequest request) {
        return Syntax.forMimeType(Objects.requireNonNullElse(request.getHeader(HttpHeaders.CONTENT_TYPE), ""))
                     .orElseThrow(() -> WpException.invalidRequest("The Content-Type is one of "
                                                                           + String.join(", ", AXIOM_MEDIA_TYPES)));
    }

    /**
     * Refuses a body that says it is too large before anything is read.
     */
    private static void requireDeclaredSizeWithinLimit(HttpServletRequest request) {
        if (request.getContentLengthLong() > MAX_DOCUMENT_BYTES) {
            throw tooLarge();
        }
    }

    private static WpException tooLarge() {
        return new WpException(PAYLOAD_TOO_LARGE, 413, "Axioms documents are at most 10 MB");
    }

    /**
     * The body in its charset (UTF-8 by default), read up to {@link #MAX_DOCUMENT_BYTES}. The service reads it once
     * the project and the caller's permission have been checked.
     *
     * @throws WpException {@value #PAYLOAD_TOO_LARGE} (413) for a larger body
     */
    private static String document(HttpServletRequest request) {
        byte[] bytes;
        try {
            bytes = request.getInputStream().readNBytes(MAX_DOCUMENT_BYTES + 1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (bytes.length > MAX_DOCUMENT_BYTES) {
            throw tooLarge();
        }
        Charset charset = StandardCharsets.UTF_8;
        var contentType = request.getContentType();
        if (contentType != null) {
            try {
                var declared = MediaType.parseMediaType(contentType).getCharset();
                if (declared != null) {
                    charset = declared;
                }
            } catch (RuntimeException e) {
                throw WpException.invalidRequest("Unreadable Content-Type " + contentType);
            }
        }
        return new String(bytes, charset);
    }

    @Nullable
    private static IRI optionalIri(@Nullable String iri) {
        return iri == null ? null : OntologyParams.iri(iri);
    }

    @Nullable
    private static String iriOf(OWLOntologyID id) {
        return id.getOntologyIRI().isPresent() ? id.getOntologyIRI().get().toString() : null;
    }

    @Nullable
    private static String versionIriOf(OWLOntologyID id) {
        return id.getVersionIRI().isPresent() ? id.getVersionIRI().get().toString() : null;
    }

    /**
     * One of the project's ontologies; an anonymous ontology has no IRI.
     */
    public record OntologyDto(@Nullable String ontologyIri, @Nullable String versionIri, String shortForm,
                              boolean root) {
    }

    /**
     * An ontology's annotations: plain, which the client edits and sends back as {@code from}, and rendered, in the
     * same order.
     */
    public record OntologyAnnotationsDto(@Nullable String ontologyIri,
                                         @Nullable String versionIri,
                                         List<PlainPropertyAnnotationValue> annotations,
                                         List<PropertyValueDto> rendered) {

        static OntologyAnnotationsDto of(OntologyAnnotations annotations) {
            return new OntologyAnnotationsDto(iriOf(annotations.ontologyId()),
                                              versionIriOf(annotations.ontologyId()),
                                              annotations.annotations(),
                                              annotations.rendered().stream().map(PropertyValueDto::of).toList());
        }
    }

    public record AnnotationsUpdateRequest(List<PlainPropertyAnnotationValue> from,
                                           List<PlainPropertyAnnotationValue> to,
                                           String commitMessage) {
    }

    public record AddedAxiomsDto(int addedAxioms) {
    }

    public record DeletedAxiomsDto(int deletedAxioms) {
    }
}
