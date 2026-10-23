package org.industrial.ontology.api.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.industrial.ontology.api.ontology.OntologyJson.ChangeOutcomeDto;
import org.industrial.ontology.api.ontology.OntologyJson.PageDto;
import org.industrial.ontology.api.ontology.OntologyParams;
import org.industrial.ontology.api.project.ProjectIds;
import org.industrial.ontology.api.security.Caller;
import org.industrial.ontology.app.error.WpException;
import org.industrial.ontology.app.revision.RevisionService;
import org.industrial.ontology.app.revision.RevisionService.ListedRevision;
import org.industrial.ontology.app.revision.RevisionService.RevisionDiff;
import org.industrial.ontology.domain.change.ProjectChange;
import org.industrial.ontology.domain.core.UserId;
import org.industrial.ontology.domain.diff.DiffElement;
import org.industrial.ontology.domain.revision.RevisionNumber;
import org.industrial.ontology.domain.revision.RevisionSummary;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A project's revisions and changes (docs/02 §5, stage S8). Revision numbers start at 1; timestamps are ISO-8601 in
 * UTC. The service checks the permissions: {@code ViewChanges} to read, {@code RevertChanges} and
 * {@code EditOntology} to revert.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@Tag(name = "Revisions", description = "The revision history, the changes of an entity and reverting a revision")
public class RevisionsController {

    private static final int MAX_CHANGES_PAGE_SIZE = 100;

    private final RevisionService revisionService;

    public RevisionsController(RevisionService revisionService) {
        this.revisionService = checkNotNull(revisionService);
    }

    @Operation(summary = "The revisions from one number to another, oldest first (permission ViewChanges)",
               description = "from defaults to 1 and to to the head; userId keeps the revisions of one author.")
    @GetMapping("/revisions")
    public List<RevisionSummaryDto> revisions(@Caller UserId caller,
                                              @PathVariable String projectId,
                                              @RequestParam(defaultValue = "1") long from,
                                              @RequestParam(required = false) Long to,
                                              @RequestParam(required = false) String userId) {
        var author = Optional.ofNullable(userId).filter(name -> !name.isBlank()).map(UserId::getUserId);
        return revisionService.getRevisions(caller, ProjectIds.parse(projectId), from, to, author)
                              .stream()
                              .map(RevisionSummaryDto::of)
                              .toList();
    }

    @Operation(summary = "The number of the latest revision, 0 if there is none (permission ViewChanges)")
    @GetMapping("/revisions/head")
    public HeadDto head(@Caller UserId caller, @PathVariable String projectId) {
        return new HeadDto(revisionService.getHeadRevision(caller, ProjectIds.parse(projectId)).getValue());
    }

    @Operation(summary = "The axioms added and removed overall from one revision to another (permission "
            + "ViewChanges)",
               description = "from is the revision before the range (default 0), to the last one (default the head); "
                       + "axioms in Manchester syntax, at most 10000 of each.")
    @GetMapping("/revisions/diff")
    public RevisionDiff diff(@Caller UserId caller,
                             @PathVariable String projectId,
                             @RequestParam(defaultValue = "0") long from,
                             @RequestParam(required = false) Long to) {
        return revisionService.getDiff(caller, ProjectIds.parse(projectId), from, to);
    }

    @Operation(summary = "A revision with the axioms it added and removed (permission ViewChanges)",
               description = "Lists at most 1000 axiom changes; changeCount counts them all.")
    @GetMapping("/revisions/{revision}")
    public RevisionDetailsDto revision(@Caller UserId caller,
                                       @PathVariable String projectId,
                                       @PathVariable String revision) {
        return RevisionDetailsDto.of(revisionService.getRevision(caller, ProjectIds.parse(projectId),
                                                                 revisionNumber(revision)));
    }

    @Operation(summary = "A page of the changes, newest first, optionally of one entity (permission ViewChanges)",
               description = "Each element is a revision with its changes rendered as a diff; with iri and type, "
                       + "only the revisions that changed that entity, with only its changes.")
    @GetMapping("/changes")
    public PageDto<ProjectChangeDto> changes(@Caller UserId caller,
                                             @PathVariable String projectId,
                                             @RequestParam(required = false) String iri,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        var entity = iri == null && type == null
                ? Optional.<OWLEntity>empty()
                : Optional.of(OntologyParams.entity(iri, type));
        return PageDto.of(revisionService.getChanges(caller, ProjectIds.parse(projectId), entity,
                                                     OntologyParams.page(page, size, MAX_CHANGES_PAGE_SIZE)),
                          ProjectChangeDto::of);
    }

    @Operation(summary = "Applies the inverse of a revision's changes as a new revision (permissions "
            + "RevertChanges and EditOntology)",
               description = "The body may give a commitMessage; the default is \"Reverted revision n\".")
    @PostMapping("/revisions/{revision}/revert")
    public ChangeOutcomeDto<RevertedDto> revert(@Caller UserId caller,
                                                @PathVariable String projectId,
                                                @PathVariable String revision,
                                                @RequestBody(required = false) RevertRequest request) {
        var number = revisionNumber(revision);
        var outcome = revisionService.revert(caller, ProjectIds.parse(projectId), number,
                                             request == null ? null : request.commitMessage());
        return ChangeOutcomeDto.of(outcome, reverted -> new RevertedDto(number.getValue()));
    }

    private static RevisionNumber revisionNumber(String revision) {
        try {
            var number = Long.parseLong(revision.strip());
            if (number >= 1) {
                return RevisionNumber.getRevisionNumber(number);
            }
        } catch (NumberFormatException e) {
            // refused below
        }
        throw WpException.invalidRequest("A revision number is a whole number from 1: " + revision);
    }

    private static String isoTimestamp(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis).toString();
    }

    public record HeadDto(long number) {
    }

    public record RevisionSummaryDto(long number,
                                     @Nonnull String timestamp,
                                     @Nonnull String userId,
                                     @Nonnull String description,
                                     int changeCount) {

        static RevisionSummaryDto of(RevisionSummary summary) {
            return new RevisionSummaryDto(summary.getRevisionNumber().getValue(),
                                          isoTimestamp(summary.getTimestamp()),
                                          summary.getUserId().getUserName(),
                                          summary.getDescription(),
                                          summary.getChangeCount());
        }
    }

    /**
     * @param subjects the IRIs of the entities whose axioms changed
     * @param changeCount the number of all the revision's changes, including those of ontology annotations and
     *                    imports, which {@code changes} does not list (as the legacy {@code RevisionDetails})
     * @param changes  the axiom changes among the first {@value RevisionService#MAX_LISTED_CHANGES} changes, each an
     *                 operation ({@code ADD} or {@code REMOVE}) and the axiom in OWL functional syntax
     * @param tags     the snapshot tags of the revision, reserved for the governance module (P3); always empty
     */
    public record RevisionDetailsDto(long number,
                                     @Nonnull String timestamp,
                                     @Nonnull String userId,
                                     @Nonnull String description,
                                     @Nonnull List<String> subjects,
                                     int changeCount,
                                     @Nonnull List<AxiomChangeDto> changes,
                                     @Nonnull List<String> tags) {

        static RevisionDetailsDto of(ListedRevision listed) {
            var details = listed.details();
            var changes = details.getChangeDetails();
            return new RevisionDetailsDto(details.getRevisionNumber(),
                                          isoTimestamp(details.getTimestamp()),
                                          details.getUserId().getUserName(),
                                          details.getMsg(),
                                          details.getSubjects().stream().map(IRI::toString).sorted().toList(),
                                          listed.changeCount(),
                                          changes.stream()
                                                 .map(change -> new AxiomChangeDto(change.getOperation().name(),
                                                                                   change.getAxiom().toString()))
                                                 .toList(),
                                          List.of());
        }
    }

    public record AxiomChangeDto(@Nonnull String operation, @Nonnull String axiom) {
    }

    /**
     * @param diff the revision's changes (with an entity, those of the entity; otherwise the first 50) as diff
     *             lines, each the operation ({@code ADD} or {@code REMOVE}), the ontology and the change rendered
     *             as HTML in which literals and IRIs are escaped
     */
    public record ProjectChangeDto(long revision,
                                   @Nonnull String userId,
                                   @Nonnull String timestamp,
                                   @Nonnull String summary,
                                   int changeCount,
                                   @Nonnull PageDto<DiffLineDto> diff) {

        static ProjectChangeDto of(ProjectChange change) {
            return new ProjectChangeDto(change.getRevisionNumber().getValue(),
                                        change.getAuthor().getUserName(),
                                        isoTimestamp(change.getTimestamp()),
                                        change.getSummary(),
                                        change.getChangeCount(),
                                        PageDto.of(change.getDiff(), DiffLineDto::of));
        }
    }

    public record DiffLineDto(@Nonnull String operation, @Nonnull String document, @Nonnull String html) {

        static DiffLineDto of(DiffElement<String, String> element) {
            return new DiffLineDto(element.getDiffOperation().name(), element.getSourceDocument(),
                                   element.getLineElement());
        }
    }

    public record RevertRequest(@Nullable String commitMessage) {
    }

    /**
     * @param reverted the number of the revision whose changes were undone
     */
    public record RevertedDto(long reverted) {
    }
}
